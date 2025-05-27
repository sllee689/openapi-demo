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
                    mkdir -p ${DOCUSAURUS_DIR}/static/icons
                '''

                // 复制已有的图片资源
                script {
                    // 检查工作区中的图片目录
                    if (fileExists("${WORKSPACE}/static/img")) {
                        sh '''
                            echo "找到图片资源目录，开始复制..."

                            # 显示源目录内容
                            echo "源目录图片文件列表:"
                            ls -la ${WORKSPACE}/static/img/

                            # 检查必要的图片文件
                            if [ ! -f "${WORKSPACE}/static/img/logo.png" ]; then
                                echo "错误: 源目录中缺少logo.png文件，可能会导致404错误"
                            else
                                echo "找到logo.png文件"
                            fi

                            if [ ! -f "${WORKSPACE}/static/img/favicon.ico" ]; then
                                echo "错误: 源目录中缺少favicon.ico文件，可能会导致404错误"
                            else
                                echo "找到favicon.ico文件"
                            fi

                            # 复制所有图片文件
                            cp -rf ${WORKSPACE}/static/img/* ${DOCUSAURUS_DIR}/static/img/

                            # 显示复制后的目标目录内容
                            echo "已复制的图片文件列表:"
                            ls -la ${DOCUSAURUS_DIR}/static/img/
                        '''
                    } else {
                        echo "警告: 未找到 ${WORKSPACE}/static/img 目录，网站可能缺少必要的图片资源"

                        // 在configs目录中寻找图片资源
                        if (fileExists("${WORKSPACE}/${CONFIGS_DIR}/static/img")) {
                            sh '''
                                echo "在configs目录找到图片资源，开始复制..."
                                mkdir -p ${DOCUSAURUS_DIR}/static/img/
                                cp -rf ${WORKSPACE}/${CONFIGS_DIR}/static/img/* ${DOCUSAURUS_DIR}/static/img/
                                echo "已复制configs中的图片资源:"
                                ls -la ${DOCUSAURUS_DIR}/static/img/

                                # 检查必要的图片文件
                                if [ ! -f "${DOCUSAURUS_DIR}/static/img/logo.png" ]; then
                                    echo "错误: 复制后缺少logo.png文件，将导致404错误"
                                fi

                                if [ ! -f "${DOCUSAURUS_DIR}/static/img/favicon.ico" ]; then
                                    echo "错误: 复制后缺少favicon.ico文件，将导致404错误"
                                fi
                            '''
                        } else {
                            echo "错误: 未找到任何图片资源目录，网站将出现404错误"
                        }
                    }
                }
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

                    // 检查图片文件是否已存在
                    sh '''
                        # 检查目标目录中图片文件
                        echo "最终检查图片文件..."
                        if [ ! -f "${DOCUSAURUS_DIR}/static/img/logo.png" ]; then
                            echo "错误: 构建目录中缺少logo.png文件，网站将出现404错误"
                            # 不创建默认图片，保留404错误以便开发者更容易发现问题
                        else
                            echo "验证: logo.png 文件已存在"
                        fi

                        if [ ! -f "${DOCUSAURUS_DIR}/static/img/favicon.ico" ]; then
                            echo "错误: 构建目录中缺少favicon.ico文件，网站将出现404错误"
                            # 不创建默认图片，保留404错误以便开发者更容易发现问题
                        else
                            echo "验证: favicon.ico 文件已存在"
                        fi
                    '''
                }
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

        stage('部署到S3') {
            steps {
                withAWS(region: 'ap-northeast-1', credentials: 'aws-credentials') {
                    sh '''
                        echo "开始部署到S3..."
                        aws s3 sync ${DOCUSAURUS_DIR}/build/ s3://web1156 --delete
                        echo "部署完成: http://web1156.s3-website-ap-northeast-1.amazonaws.com"
                    '''
                }
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