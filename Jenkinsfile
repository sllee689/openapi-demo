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
       // 在"准备环境"阶段中添加或替换现有的图片目录创建代码
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
                   if (fileExists("${WORKSPACE}/static/img")) {
                       sh '''
                           echo "复制已有图片资源..."
                           cp -rf ${WORKSPACE}/static/img/* ${DOCUSAURUS_DIR}/static/img/
                           echo "图片资源已复制"

                           # 显示已复制的图片列表
                           echo "已复制的图片文件:"
                           ls -la ${DOCUSAURUS_DIR}/static/img/
                       '''
                   } else {
                       echo "警告: 未找到 static/img 目录，跳过图片复制"
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
                    # 初始化英文文档路径变量
                    REST_EN_DOC_PATH=""
                    WEBSOCKET_EN_DOC_PATH=""

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

                    # 查找英文文档
                    if [ -f "${WORKSPACE}/${CONFIGS_DIR}/OPENAPI-SPOT-REST-EN.md" ]; then
                        REST_EN_DOC_PATH="${WORKSPACE}/${CONFIGS_DIR}/OPENAPI-SPOT-REST-EN.md"
                        echo "在configs目录找到英文REST API文档"
                    fi

                    if [ -f "${WORKSPACE}/${CONFIGS_DIR}/OPENAPI-SPOT-WEBSOCKET-EN.md" ]; then
                        WEBSOCKET_EN_DOC_PATH="${WORKSPACE}/${CONFIGS_DIR}/OPENAPI-SPOT-WEBSOCKET-EN.md"
                        echo "在configs目录找到英文WebSocket API文档"
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

                    # 查找英文文档
                    if [ -z "$REST_EN_DOC_PATH" ] && [ -f "${WORKSPACE}/OPENAPI-SPOT-REST-EN.md" ]; then
                        REST_EN_DOC_PATH="${WORKSPACE}/OPENAPI-SPOT-REST-EN.md"
                        echo "在根目录找到英文REST API文档"
                    fi

                    if [ -z "$WEBSOCKET_EN_DOC_PATH" ] && [ -f "${WORKSPACE}/OPENAPI-SPOT-WEBSOCKET-EN.md" ]; then
                        WEBSOCKET_EN_DOC_PATH="${WORKSPACE}/OPENAPI-SPOT-WEBSOCKET-EN.md"
                        echo "在根目录找到英文WebSocket API文档"
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

                        # 查找英文文档
                        if [ -z "$REST_EN_DOC_PATH" ] && [ -f "${DIR}/OPENAPI-SPOT-REST-EN.md" ]; then
                            REST_EN_DOC_PATH="${DIR}/OPENAPI-SPOT-REST-EN.md"
                            echo "在${DIR}找到英文REST API文档"
                        fi

                        if [ -z "$WEBSOCKET_EN_DOC_PATH" ] && [ -f "${DIR}/OPENAPI-SPOT-WEBSOCKET-EN.md" ]; then
                            WEBSOCKET_EN_DOC_PATH="${DIR}/OPENAPI-SPOT-WEBSOCKET-EN.md"
                            echo "在${DIR}找到英文WebSocket API文档"
                        fi
                    done

                    # 文档路径存储到临时文件，供后续步骤使用
                    echo "${REST_DOC_PATH}" > rest_doc_path.txt
                    echo "${WEBSOCKET_DOC_PATH}" > websocket_doc_path.txt
                    echo "${REST_EN_DOC_PATH}" > rest_en_doc_path.txt
                    echo "${WEBSOCKET_EN_DOC_PATH}" > websocket_en_doc_path.txt

                    echo "REST文档路径: ${REST_DOC_PATH}"
                    echo "WebSocket文档路径: ${WEBSOCKET_DOC_PATH}"
                    echo "英文REST文档路径: ${REST_EN_DOC_PATH}"
                    echo "英文WebSocket文档路径: ${WEBSOCKET_EN_DOC_PATH}"
                '''
            }
        }

        stage('处理API文档') {
            steps {
                 sh '''
                            # 获取之前查找到的文档路径
                            REST_DOC_PATH=$(cat rest_doc_path.txt)
                            WEBSOCKET_DOC_PATH=$(cat websocket_doc_path.txt)
                            REST_EN_DOC_PATH=$(cat rest_en_doc_path.txt)
                            WEBSOCKET_EN_DOC_PATH=$(cat websocket_en_doc_path.txt)

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

                            # 创建i18n目录结构
                            mkdir -p ${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api

                            # 处理英文REST API文档
                            if [ -n "$REST_EN_DOC_PATH" ] && [ -f "$REST_EN_DOC_PATH" ]; then
                                cp -f "${REST_EN_DOC_PATH}" "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api/rest.md"
                                echo "已复制英文REST API文档: ${REST_EN_DOC_PATH} -> ${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api/rest.md"
                                # 添加前置元数据
                                sed -i '1i ---\\ntitle: REST API\\ndescription: MGBX REST API Documentation\\n---\\n' "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api/rest.md"
                                echo "已添加英文REST文档前置元数据"
                            else
                                echo "警告: 未找到英文REST API文档，创建空文档"
                                echo -e "---\\ntitle: REST API\\ndescription: MGBX REST API Documentation\\n---\\n\\n# REST API\\n\\nDocumentation is being updated..." > "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api/rest.md"
                            fi

                            # 处理英文WebSocket API文档
                            if [ -n "$WEBSOCKET_EN_DOC_PATH" ] && [ -f "$WEBSOCKET_EN_DOC_PATH" ]; then
                                cp -f "${WEBSOCKET_EN_DOC_PATH}" "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api/websocket.md"
                                echo "已复制英文WebSocket API文档: ${WEBSOCKET_EN_DOC_PATH} -> ${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api/websocket.md"
                                # 添加前置元数据
                                sed -i '1i ---\\ntitle: WebSocket API\\ndescription: MGBX WebSocket API Documentation\\n---\\n' "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api/websocket.md"
                                echo "已添加英文WebSocket文档前置元数据"
                            else
                                echo "警告: 未找到英文WebSocket API文档，创建空文档"
                                echo -e "---\\ntitle: WebSocket API\\ndescription: MGBX WebSocket API Documentation\\n---\\n\\n# WebSocket API\\n\\nDocumentation is being updated..." > "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api/websocket.md"
                            fi

                            # 创建英文首页文档
                            cat > "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/intro.md" << EOF
                ---
                id: intro
                slug: /
                title: MGBX API Documentation
                ---

                # MGBX API Documentation Center

                Welcome to the MGBX Trading Platform API. This documentation provides detailed integration guides.

                You can check our integration examples and the latest documentation updates in our [GitHub Repository](https://github.com/megabit-open/openapi-spot-docs).

                ## Documentation Navigation

                - [REST API](api/rest) - HTTP interfaces for trading, account management, and other operations
                - [WebSocket API](api/websocket) - Real-time data streaming for market data

                ## Quick Start

                1. [Understand the signature algorithm](api/rest#signature-algorithm)
                2. [Start using the API](api/rest#trading-interfaces)

                EOF

                            # 复制侧边栏配置到英文i18n目录
                            mkdir -p ${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current
                            cat > "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/sidebar-label.json" << EOF
                {
                  "version.label": {
                    "message": "Current",
                    "description": "The label for version current"
                  }
                }
                EOF

                            # 创建英文版导航翻译
                            mkdir -p ${DOCUSAURUS_DIR}/i18n/en/docusaurus-theme-classic
                            cat > "${DOCUSAURUS_DIR}/i18n/en/docusaurus-theme-classic/navbar.json" << EOF
                {
                  "title": {
                    "message": "MGBX API Docs",
                    "description": "The title in the navbar"
                  },
                  "item.label.GitHub": {
                    "message": "GitHub",
                    "description": "Navbar item with label GitHub"
                  },
                  "item.label.官网": {
                    "message": "Official Website",
                    "description": "Navbar item with label 官网"
                  }
                }
                EOF
                        '''

                // 复制配置文件（这部分不变）
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

        stage('部署到S3') {
            steps {
                withAWS(region: 'ap-northeast-1', credentials: 'aws-credentials') {
                    sh '''
                        echo "开始部署到S3..."
                        aws s3 sync ${DOCUSAURUS_DIR}/build/ s3://web1156 --delete
                        echo "部署完成: http://web1156.s3-website-ap-northeast-1.amazonaws.com"

                        # 如果有CloudFront分配，可以刷新缓存
                        # aws cloudfront create-invalidation --distribution-id YOUR_DISTRIBUTION_ID --paths "/*"
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
           sh "rm -f rest_doc_path.txt websocket_doc_path.txt rest_en_doc_path.txt websocket_en_doc_path.txt || true"
       }
   }
}