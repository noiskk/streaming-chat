services:
  sw_team_1-backend-blue:
    image: chat-backend:latest
    container_name: sw_team_1-backend-blue
    ports:
      - "8080:8080"

  sw_team_1-backend-green:
    image: chat-backend:latest
    container_name: sw_team_1-backend-green
    ports:
      - "8081:8080"

  sw_team_1-frontend:
    image: chat-frontend:latest
    container_name: sw_team_1-frontend
    ports:
      - "3000:80"

  sw_team_1-nginx:
    image: nginx:alpine
    container_name: sw_team_1-nginx
    ports:
      - "8002:8002"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/conf.d/default.conf
    depends_on:
      - sw_team_1-backend-blue
      - sw_team_1-frontend
