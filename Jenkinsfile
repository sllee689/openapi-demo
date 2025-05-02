pipeline {
    agent any

    tools {
        nodejs "node23"
        maven "M3"
    }

    environment {
        DOCUSAURUS_DIR = "website"
    }

    stages {
        stage('准备环境') {
            steps {
                echo "准备构建环境"
                sh 'node -v && npm -v'

                // 清理并重建目录
                sh '''
                    rm -rf ${DOCUSAURUS_DIR}
                    mkdir -p ${DOCUSAURUS_DIR}/docs/api
                    mkdir -p ${DOCUSAURUS_DIR}/src/css
                    mkdir -p ${DOCUSAURUS_DIR}/static/img
                '''
            }
        }

        stage('处理API文档') {
            steps {
                sh '''
                    # 复制API文档
                    cp -f ${WORKSPACE}/docs/OPENAPI-SPOT-REST.md ${DOCUSAURUS_DIR}/docs/api/rest.md
                    cp -f ${WORKSPACE}/docs/OPENAPI-SPOT-WEBSOCKET.md ${DOCUSAURUS_DIR}/docs/api/websocket.md

                    # 添加前置元数据
                    sed -i '1i ---\\ntitle: REST API\\ndescription: MGBX REST API接入文档\\n---\\n' ${DOCUSAURUS_DIR}/docs/api/rest.md
                    sed -i '1i ---\\ntitle: WebSocket API\\ndescription: MGBX WebSocket API接入文档\\n---\\n' ${DOCUSAURUS_DIR}/docs/api/websocket.md

                    # 创建首页文档
                    cat > ${DOCUSAURUS_DIR}/docs/intro.md << EOF
---
id: intro
slug: /
title: MGBX API 文档中心
---

# MGBX API 文档中心

欢迎使用 MGBX 交易平台 API。本文档提供了详细的接入指南。

## 文档导航

- [REST API](api/rest) - HTTP 接口，用于交易、账户管理等操作
- [WebSocket API](api/websocket) - 实时数据推送，用于行情订阅

## 快速开始

1. [创建 API 密钥](api/rest#认证机制)
2. [了解签名算法](api/rest#签名算法)
3. [开始使用 API](api/rest#交易接口)

EOF
                '''

                // 复制配置文件 - 从configs目录读取
                writeFile file: "${DOCUSAURUS_DIR}/docusaurus.config.js", text: readFile("${WORKSPACE}/configs/docusaurus.config.js")
                writeFile file: "${DOCUSAURUS_DIR}/sidebars.js", text: readFile("${WORKSPACE}/configs/sidebars.js")
                writeFile file: "${DOCUSAURUS_DIR}/src/css/custom.css", text: readFile("${WORKSPACE}/configs/custom.css")

                // 创建简单logo
                sh '''
                    cat > ${DOCUSAURUS_DIR}/static/img/logo.svg << EOF
<svg width="200" height="200" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
  <rect width="200" height="200" fill="#ffffff"/>
  <text x="50%" y="50%" font-family="Arial" font-size="48" text-anchor="middle" fill="#2e8555">MGBX</text>
</svg>
EOF
                    # 创建空favicon
                    touch ${DOCUSAURUS_DIR}/static/img/favicon.ico
                '''
            }
        }

        stage('构建文档站') {
            steps {
                dir("${DOCUSAURUS_DIR}") {
                    // 创建package.json
                    sh '''
                        cat > package.json << EOF
{
  "name": "mgbx-api-docs",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "docusaurus": "docusaurus",
    "start": "docusaurus start",
    "build": "docusaurus build",
    "serve": "docusaurus serve"
  },
  "dependencies": {
    "@docusaurus/core": "2.4.3",
    "@docusaurus/preset-classic": "2.4.3",
    "@mdx-js/react": "^1.6.22",
    "clsx": "^1.2.1",
    "prism-react-renderer": "^1.3.5",
    "react": "^17.0.2",
    "react-dom": "^17.0.2"
  },
  "browserslist": {
    "production": [">0.5%", "not dead", "not op_mini all"],
    "development": ["last 1 chrome version", "last 1 firefox version", "last 1 safari version"]
  }
}
EOF
                        # 安装依赖和构建
                        npm install
                        npm run build
                    '''
                }
            }
        }

        stage('归档和部署') {
            steps {
                echo "归档构建结果"
                archiveArtifacts artifacts: "${DOCUSAURUS_DIR}/build/**", fingerprint: true
            }
        }
    }

    post {
        success {
            echo "构建成功：文档站已生成，请部署 ${DOCUSAURUS_DIR}/build 目录"
        }
        failure {
            echo "构建失败，请检查日志"
        }
    }
}