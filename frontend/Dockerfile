# Dockerfile para a aplicação Angular (Frontend)

# Estágio 1: Build da aplicação Angular
# Use uma imagem Node com a versão do Angular CLI que você está usando (ex: Node 18)
FROM node:18-alpine AS build

# Define o diretório de trabalho
WORKDIR /app

# Copia package.json e package-lock.json (ou yarn.lock)
COPY package.json package-lock.json* yarn.lock* ./

# Instala as dependências
# Se estiver usando npm:
RUN npm install
# Se estiver usando yarn:
# RUN yarn install

# Copia todo o código fonte da aplicação
COPY . .

# Argumento para passar a URL da API durante o build
ARG API_BASE_URL=http://localhost:8080/api
ENV API_BASE_URL=${API_BASE_URL}

# Build da aplicação para produção
# O output path é geralmente `dist/<project-name>`
# Verifique seu angular.json outputPath se for diferente
RUN npm run build -- --configuration production --set "$API_BASE_URL"
# Se estiver usando yarn:
# RUN yarn build --configuration production

# Estágio 2: Servir a aplicação com Nginx
FROM nginx:1.25-alpine

# Remove a configuração padrão do Nginx
RUN rm /etc/nginx/conf.d/default.conf

# Copia a configuração customizada do Nginx (será criada a seguir)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copia os artefatos da build do estágio anterior para o diretório do Nginx
# Substitua 'unihub-frontend' pelo nome do seu projeto Angular se for diferente (ver angular.json "defaultProject")
COPY --from=build /app/dist/unihub /usr/share/nginx/html

# Expõe a porta 80 (Nginx padrão)
EXPOSE 80

# Comando para iniciar o Nginx
CMD ["nginx", "-g", "daemon off;"] 