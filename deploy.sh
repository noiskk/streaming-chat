#!/bin/bash
set -e

# 0. 변수 설정
IMAGE_TAG=${1:-latest}
HEALTH_TIMEOUT=60
BLUE="sw_team_1-backend-blue"   # 블루 컨테이너명
GREEN="sw_team_1-backend-green" # 그린 컨테이너명
NGINX="sw_team_1-nginx"         # Nginx 컨테이너명
IMAGE_NAME="sw_team_1-backend"  # 이미지명

# [중요] Nginx 컨테이너가 실제로 마운트해서 사용 중인 호스트의 절대 경로
REAL_NGINX_CONF="/home/sw_team_1/streaming-chat/nginx/nginx.conf"

# 1. 현재 활성 환경 확인
if docker ps --filter "name=$BLUE" --filter "status=running" | grep -q $BLUE; then
    ACTIVE=$BLUE
    INACTIVE=$GREEN
    ACTIVE_PORT=8080
    INACTIVE_PORT=8081
else
    ACTIVE=$GREEN
    INACTIVE=$BLUE
    ACTIVE_PORT=8081
    INACTIVE_PORT=8080
fi

echo "[1] 현재 활성: $ACTIVE → 배포 대상: $INACTIVE"

# 2. 비활성 컨테이너 교체
echo "[2] $INACTIVE 컨테이너 시작 준비"
docker stop $INACTIVE 2>/dev/null || true
docker rm   $INACTIVE 2>/dev/null || true

docker run -d \
  --name $INACTIVE \
  --network streaming-chat_default \
  -p ${INACTIVE_PORT}:8080 \
  -e SPRING_AI_GOOGLE_GENAI_API_KEY=${GEMINI_API_KEY} \
  ${IMAGE_NAME}:${IMAGE_TAG}

# 3. 헬스체크
echo "[3] 헬스체크 대기 중..."
# 컨테이너 IP 추출
CONTAINER_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $INACTIVE)
echo "    대상 컨테이너 IP: $CONTAINER_IP"

for i in $(seq 1 $HEALTH_TIMEOUT); do
    # Jenkins가 같은 네트워크에 있다면 IP:8080으로 직접 호출 가능
    RESULT=$(curl -s http://${CONTAINER_IP}:8080/actuator/health 2>/dev/null || true)
    
    if echo "$RESULT" | grep -q '"UP"'; then
        echo "    → 정상 기동 확인 (${i}초)"
        break
    fi
    
    if [ $i -eq $HEALTH_TIMEOUT ]; then
        echo "    → [에러] 60초간 헬스체크 실패. 배포를 중단합니다."
        docker logs $INACTIVE | tail -n 20
        exit 1
    fi
    sleep 1
done

# 4. Nginx 트래픽(upstream) 전환
echo "[4] Nginx 설정 업데이트 시작"

if [ -f "$REAL_NGINX_CONF" ]; then
    # 절대 경로에 있는 실제 파일을 수정
    sed -i "s/${ACTIVE}/${INACTIVE}/g" "$REAL_NGINX_CONF"
    
    # Nginx 컨테이너에게 설정 재로드 명령
    if docker exec $NGINX nginx -t; then
        docker exec $NGINX nginx -s reload
        echo "    → Nginx 전환 완료: $ACTIVE → $INACTIVE"
    else
        echo "    → [에러] Nginx 설정 문법 오류!"
        exit 1
    fi
else
    echo "    → [에러] Nginx 설정 파일을 찾을 수 없습니다: $REAL_NGINX_CONF"
    exit 1
fi

# 5. 구버전 컨테이너 종료 (Graceful Shutdown)
echo "[5] 60초 대기 후 구버전($ACTIVE) 종료..."
docker stop --time=60 $ACTIVE 2>/dev/null || true

# 6. 오래된 이미지 정리
echo "[6] 이미지 정리 중..."
docker images ${IMAGE_NAME} --format "{{.Tag}}" \
  | grep -v latest \
  | sort -n \
  | head -n -2 \
  | xargs -I {} docker rmi ${IMAGE_NAME}:{} 2>/dev/null || true

echo "[완료] 무중단 배포가 성공적으로 마무리되었습니다."
