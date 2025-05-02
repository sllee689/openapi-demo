pipeline {
    agent any

    tools {
        nodejs "node23"
        maven "M3"
    }

    environment {
        DOCUSAURUS_DIR = "website"
        API_DOCS_DIR = "${DOCUSAURUS_DIR}/docs/api"
        CONFIGS_DIR = "configs"
    }

    stages {
        stage('准备环境') {
            steps {
                echo "准备构建环境"
                sh 'node -v && npm -v'

                // 清理并重建目录
                sh '''
                    rm -rf ${DOCUSAURUS_DIR}
                    mkdir -p ${API_DOCS_DIR}
                    mkdir -p ${DOCUSAURUS_DIR}/src/css
                    mkdir -p ${DOCUSAURUS_DIR}/static/img
                '''
            }
        }

        stage('查找API文档') {
            steps {
                echo "查找API文档文件..."
                sh '''
                    # 列出所有markdown文件便于调试
                    echo "所有Markdown文件列表:"
                    find ${WORKSPACE} -name "*.md" -type f | sort

                    # 初始化文档路径变量
                    REST_DOC_PATH=""
                    WEBSOCKET_DOC_PATH=""

                    # 从最可能到最不可能的位置查找文档
                    # 1. 首先查找configs目录
                    if [ -f "${WORKSPACE}/${CONFIGS_DIR}/OPENAPI-SPOT-REST.md" ]; then
                        REST_DOC_PATH="${WORKSPACE}/${CONFIGS_DIR}/OPENAPI-SPOT-REST.md"
                        echo "在configs目录找到REST API文档"
                    fi

                    if [ -f "${WORKSPACE}/${CONFIGS_DIR}/OPENAPI-SPOT-WEBSOCKET.md" ]; then
                        WEBSOCKET_DOC_PATH="${WORKSPACE}/${CONFIGS_DIR}/OPENAPI-SPOT-WEBSOCKET.md"
                        echo "在configs目录找到WebSocket API文档"
                    fi

                    # 2. 检查项目根目录
                    if [ -z "$REST_DOC_PATH" ] && [ -f "${WORKSPACE}/OPENAPI-SPOT-REST.md" ]; then
                        REST_DOC_PATH="${WORKSPACE}/OPENAPI-SPOT-REST.md"
                        echo "在根目录找到REST API文档"
                    fi

                    if [ -z "$WEBSOCKET_DOC_PATH" ] && [ -f "${WORKSPACE}/OPENAPI-SPOT-WEBSOCKET.md" ]; then
                        WEBSOCKET_DOC_PATH="${WORKSPACE}/OPENAPI-SPOT-WEBSOCKET.md"
                        echo "在根目录找到WebSocket API文档"
                    fi

                    # 3. 检查其他可能的目录
                    for DIR in "${WORKSPACE}/docs" "${WORKSPACE}/src/docs" "${WORKSPACE}/src/main/resources/docs" "${WORKSPACE}/sdk/docs"; do
                        if [ -z "$REST_DOC_PATH" ] && [ -f "${DIR}/OPENAPI-SPOT-REST.md" ]; then
                            REST_DOC_PATH="${DIR}/OPENAPI-SPOT-REST.md"
                            echo "在${DIR}找到REST API文档"
                        fi

                        if [ -z "$WEBSOCKET_DOC_PATH" ] && [ -f "${DIR}/OPENAPI-SPOT-WEBSOCKET.md" ]; then
                            WEBSOCKET_DOC_PATH="${DIR}/OPENAPI-SPOT-WEBSOCKET.md"
                            echo "在${DIR}找到WebSocket API文档"
                        fi
                    done

                    # 文档路径存储到临时文件，供后续步骤使用
                    echo "${REST_DOC_PATH}" > rest_doc_path.txt
                    echo "${WEBSOCKET_DOC_PATH}" > websocket_doc_path.txt

                    echo "REST文档路径: ${REST_DOC_PATH}"
                    echo "WebSocket文档路径: ${WEBSOCKET_DOC_PATH}"
                '''
            }
        }

        stage('处理API文档') {
            steps {
                sh '''
                    # 获取之前查找到的文档路径
                    REST_DOC_PATH=$(cat rest_doc_path.txt)
                    WEBSOCKET_DOC_PATH=$(cat websocket_doc_path.txt)

                    # 处理REST API文档
                    if [ -n "$REST_DOC_PATH" ] && [ -f "$REST_DOC_PATH" ]; then
                        cp -f "${REST_DOC_PATH}" "${API_DOCS_DIR}/rest.md"
                        echo "已复制REST API文档: ${REST_DOC_PATH} -> ${API_DOCS_DIR}/rest.md"
                        # 添加前置元数据
                        sed -i '1i ---\\ntitle: REST API\\ndescription: MGBX REST API接入文档\\n---\\n' "${API_DOCS_DIR}/rest.md"
                        echo "已添加REST文档前置元数据"
                    else
                        echo "警告: 未找到REST API文档，创建空文档"
                        echo -e "---\\ntitle: REST API\\ndescription: MGBX REST API接入文档\\n---\\n\\n# REST API\\n\\n文档正在更新中..." > "${API_DOCS_DIR}/rest.md"
                    fi

                    # 处理WebSocket API文档
                    if [ -n "$WEBSOCKET_DOC_PATH" ] && [ -f "$WEBSOCKET_DOC_PATH" ]; then
                        cp -f "${WEBSOCKET_DOC_PATH}" "${API_DOCS_DIR}/websocket.md"
                        echo "已复制WebSocket API文档: ${WEBSOCKET_DOC_PATH} -> ${API_DOCS_DIR}/websocket.md"
                        # 添加前置元数据
                        sed -i '1i ---\\ntitle: WebSocket API\\ndescription: MGBX WebSocket API接入文档\\n---\\n' "${API_DOCS_DIR}/websocket.md"
                        echo "已添加WebSocket文档前置元数据"
                    else
                        echo "警告: 未找到WebSocket API文档，创建空文档"
                        echo -e "---\\ntitle: WebSocket API\\ndescription: MGBX WebSocket API接入文档\\n---\\n\\n# WebSocket API\\n\\n文档正在更新中..." > "${API_DOCS_DIR}/websocket.md"
                    fi

                    # 创建首页文档
                    cat > "${DOCUSAURUS_DIR}/docs/intro.md" << EOF
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

                // 复制配置文件
                script {
                    // 确保配置文件存在后再复制
                    if (fileExists("${WORKSPACE}/${CONFIGS_DIR}/docusaurus.config.js")) {
                        sh "cp -f ${WORKSPACE}/${CONFIGS_DIR}/docusaurus.config.js ${DOCUSAURUS_DIR}/"
                        echo "已复制docusaurus配置文件"
                    } else {
                        error "缺少必要的配置文件: ${CONFIGS_DIR}/docusaurus.config.js"
                    }

                    if (fileExists("${WORKSPACE}/${CONFIGS_DIR}/sidebars.js")) {
                        sh "cp -f ${WORKSPACE}/${CONFIGS_DIR}/sidebars.js ${DOCUSAURUS_DIR}/"
                        echo "已复制侧边栏配置文件"
                    } else {
                        error "缺少必要的配置文件: ${CONFIGS_DIR}/sidebars.js"
                    }

                    if (fileExists("${WORKSPACE}/${CONFIGS_DIR}/custom.css")) {
                        sh "cp -f ${WORKSPACE}/${CONFIGS_DIR}/custom.css ${DOCUSAURUS_DIR}/src/css/"
                        echo "已复制自定义CSS文件"
                    } else {
                        error "缺少必要的配置文件: ${CONFIGS_DIR}/custom.css"
                    }
                }

                // 创建简单logo和favicon
                sh '''
                    cat > "${DOCUSAURUS_DIR}/static/img/logo.svg" << EOF
<svg width="200" height="200" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
  <rect width="200" height="200" fill="#ffffff"/>
  <text x="50%" y="50%" font-family="Arial" font-size="48" text-anchor="middle" fill="#2e8555">MGBX</text>
</svg>
EOF
                    # 创建空favicon
                    touch "${DOCUSAURUS_DIR}/static/img/favicon.ico"
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
                        echo "开始安装依赖..."
                        npm install
                        echo "开始构建文档站..."
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
        always {
            // 清理临时文件
            sh "rm -f rest_doc_path.txt websocket_doc_path.txt || true"
        }
    }
}