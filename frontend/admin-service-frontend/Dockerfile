FROM nginx:1.28-alpine

COPY admin-service-frontend/nginx.conf /etc/nginx/conf.d/default.conf

COPY common/ /usr/share/nginx/html/common/
COPY admin-service-frontend/index.html /usr/share/nginx/html/
COPY admin-service-frontend/css/ /usr/share/nginx/html/css/
COPY admin-service-frontend/js/ /usr/share/nginx/html/js/

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]