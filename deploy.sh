#!/bin/bash
set -e

# 0. 변수 설정
IMAGE_TAG=${1:-latest}
HEALTH_TIMEOUT=60
BLUE="sw_team_1-backend-blue"   # 블루 컨테이너명
GREEN="sw_team_1-backend-green" # 그린 컨테이너명
NGINX="sw_team_1-nginx"         # Nginx 컨테이너명
IMAGE_NAME="chat-backend"       # 이미지명

# 1. 현재 활성 환경 확인
# if sw_team_1-backend-blue 컨테이너가 실행 중이면 -> blue가 운영중, green에 배포
# else -> green이 운영중, blue에 배포
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
# 기존 비활성 컨테이너 제거 후 새 이미지로 재실행
docker stop $INACTIVE 2>/dev/null || true
docker rm   $INACTIVE 2>/dev/null || true
docker run -d \
  --name $INACTIVE \
  --network streaming-chat_default \
  -p ${INACTIVE_PORT}:8080 \
  ${IMAGE_NAME}:${IMAGE_TAG}

echo "[2] $INACTIVE 컨테이너 시작"

# 3. 헬스체크
# 1초마다 /actuator/health 호출 -> UP 응답 시 통과
# 60초 안에 응답 없으면 새 컨테이너 제거 후 종료 (자동 롤백)
echo "[3] 헬스체크 대기 중..."
for i in $(seq 1 $HEALTH_TIMEOUT); do
    RESULT=$(curl -s http://localhost:${INACTIVE_PORT}/actuator/health 2>/dev/null || true)
    if echo "$RESULT" | grep -q '"UP"'; then
        echo "    → 정상 기동 (${i}초)"
        break
    fi
    if [ $i -eq $HEALTH_TIMEOUT ]; then
        echo "    → 헬스체크 실패. 롤백"
        docker stop $INACTIVE && docker rm $INACTIVE
        exit 1
    fi
    sleep 1
done

# 4. Nginx 트래픽(upstream) 전환
# sed로 nginx.conf의 upstream 컨테이너명 교체
# nginx -t 로 문법 검증, nginx -s reload로 무중단 반영
sed -i "s/${ACTIVE}/${INACTIVE}/" ./nginx/nginx.conf
docker exec $NGINX nginx -t
docker exec $NGINX nginx -s reload
echo "[4] Nginx 전환 완료: $ACTIVE → $INACTIVE"

# 5. 구버전 컨테이너 종료 (없으면 무시)
echo "[5] 60초 대기 후 $ACTIVE 종료..."
docker stop --time=60 $ACTIVE 2>/dev/null || true
echo "[완료] 배포 성공"
