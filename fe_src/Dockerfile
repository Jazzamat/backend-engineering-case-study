FROM node:14-slim

WORKDIR /usr/src/app

COPY package*.json ./

# Install all dependencies including devDependencies
RUN npm install

COPY . .

# Set the environment variable after installation
ENV NODE_ENV production

EXPOSE 8080

CMD ["npm", "start"]
