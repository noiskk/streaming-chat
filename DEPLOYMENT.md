# DevOps & Blue-Green Deployment

Spring AI 기반 Gemini 스트리밍 챗봇 서비스에  
**Jenkins + SonarQube + Docker + Nginx를 활용한 CI/CD 및 Blue-Green 무중단 배포 환경**을 구축했습니다.

단순 기능 구현을 넘어,  
**네트워크, 프록시, 컨테이너, 스트리밍 환경까지 포함한 실제 운영 수준의 인프라 문제를 직접 해결**하며 전체 흐름을 경험했습니다.

<br />

## 📌 포트 구성

| Port | Service |
|------|--------|
| 8000 | SonarQube |
| 8001 | Jenkins |
| 8002 | Nginx |
| 8080 | Spring Server (BLUE) |
| 8081 | Spring Server (GREEN) |
| 8082 | React |
| 8099 | Jenkins Agent |

<br />

## 🔄 배포 전략: Blue-Green 무중단 배포

### 개념

운영 환경을 **Blue(구버전)** / **Green(신버전)** 두 개로 운영하고,  
신버전이 준비되면 **트래픽을 한 번에 전환하는 무중단 배포 전략**


### 장점

- 서비스 중단 없이 배포 가능  
- 장애 발생 시 즉시 롤백 가능  
- 실제 운영 환경에서 사전 검증 가능  


### 단점

- 인프라 자원이 2배 필요  
- DB 스키마 변경 시 호환성 고려 필요  

<br />

## ⚙️ CI/CD 파이프라인
<img width="800" height="678" alt="스크린샷 2026-04-15 오후 5 45 45" src="https://github.com/user-attachments/assets/e33e82e4-b8ee-440f-a145-4af65085ce1e" />

<br />

### 📌 deploy.sh 동작 흐름

1. 현재 활성 환경(Blue or Green) 확인  
2. 비활성 컨테이너에 새 버전 배포  
3. `/actuator/health` 헬스체크 수행  
4. Nginx upstream 변경 → 트래픽 전환  
5. 기존 컨테이너 60초 유지 후 종료 (Connection Draining)  

<br />

### Nginx 설정

```nginx
proxy_buffering off;
proxy_cache off;
proxy_read_timeout 300s;
```

- 스트리밍 응답 지연 방지
- 실시간 토큰 단위 응답 유지


