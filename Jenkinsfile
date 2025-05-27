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
                            echo "在 ${DIR} 目录找到REST API文档"
                        fi

                        if [ -z "$WEBSOCKET_DOC_PATH" ] && [ -f "${DIR}/OPENAPI-SPOT-WEBSOCKET.md" ]; then
                            WEBSOCKET_DOC_PATH="${DIR}/OPENAPI-SPOT-WEBSOCKET.md"
                            echo "在 ${DIR} 目录找到WebSocket API文档"
                        fi

                        # 查找英文文档
                        if [ -z "$REST_EN_DOC_PATH" ] && [ -f "${DIR}/OPENAPI-SPOT-REST-EN.md" ]; then
                            REST_EN_DOC_PATH="${DIR}/OPENAPI-SPOT-REST-EN.md"
                            echo "在 ${DIR} 目录找到英文REST API文档"
                        fi

                        if [ -z "$WEBSOCKET_EN_DOC_PATH" ] && [ -f "${DIR}/OPENAPI-SPOT-WEBSOCKET-EN.md" ]; then
                            WEBSOCKET_EN_DOC_PATH="${DIR}/OPENAPI-SPOT-WEBSOCKET-EN.md"
                            echo "在 ${DIR} 目录找到英文WebSocket API文档"
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

        // 处理API文档阶段
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

                    # 创建默认语言(中文)首页文档
                    echo "创建中文版intro.md..."
                    cat > "${DOCUSAURUS_DIR}/docs/intro.md" << EOF
---
id: intro
slug: /
title: MGBX API 文档中心
---

# MGBX API 文档中心

欢迎使用 MGBX 交易平台 API。本文档提供了详细的接入指南。

您可以在我们的 [GitHub 仓库](https://github.com/megabit-open/openapi-spot-docs) 中查看接入示例和最新的文档更新。

## 文档导航

- [REST API](api/rest) - HTTP 接口，用于交易、账户管理等操作
- [WebSocket API](api/websocket) - 实时数据推送，用于行情订阅

## 快速开始

1. [了解签名算法](api/rest#签名算法)
2. [开始使用 API](api/rest#交易接口)
EOF

                    # 创建i18n目录结构
                    echo "创建国际化目录结构..."
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
                    echo "创建英文版intro.md..."
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

                    # 创建英文侧边栏翻译
                    echo "创建英文侧边栏翻译..."
                    mkdir -p ${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs
                    cp -f ${DOCUSAURUS_DIR}/sidebars.js ${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/ || echo "无法复制侧边栏配置文件"

                    cat > "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current.json" << EOF
{
  "version.label": {
    "message": "Current",
    "description": "The label for version current"
  },
  "sidebar.apiSidebar.category.现货": {
    "message": "Spot",
    "description": "The label for category 现货 in sidebar apiSidebar"
  },
  "sidebar.apiSidebar.category.REST API": {
    "message": "REST API",
    "description": "The label for category REST API in sidebar apiSidebar"
  },
  "sidebar.apiSidebar.category.WebSocket API": {
    "message": "WebSocket API",
    "description": "The label for category WebSocket API in sidebar apiSidebar"
  },
  "sidebar.apiSidebar.doc.概述": {
    "message": "Overview",
    "description": "The label for doc 概述 in sidebar apiSidebar"
  }
}
EOF

                    # 创建英文版导航翻译
                    echo "创建英文导航翻译..."
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

                    # 创建英文版主题翻译 - 使用shell变量替代JavaScript表达式
                    echo "创建英文主题翻译..."
                    CURRENT_YEAR=$(date +%Y)
                    cat > "${DOCUSAURUS_DIR}/i18n/en/docusaurus-theme-classic/footer.json" << EOF
{
  "copyright": {
    "message": "Copyright © ${CURRENT_YEAR} MGBX. All rights reserved.",
    "description": "The footer copyright"
  }
}
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
                    # 创建基本的logo和favicon
                    cat > "${DOCUSAURUS_DIR}/static/img/logo.svg" << EOF
<svg width="200" height="200" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
  <rect x="50" y="50" width="100" height="100" fill="#2e8555" rx="10" ry="10" />
  <text x="100" y="110" font-family="Arial" font-size="40" text-anchor="middle" fill="white">MGBX</text>
</svg>
EOF

                    # 复制为PNG格式
                    cat > "${DOCUSAURUS_DIR}/static/img/logo.png" << EOF
<svg width="200" height="200" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
  <rect x="50" y="50" width="100" height="100" fill="#2e8555" rx="10" ry="10" />
  <text x="100" y="110" font-family="Arial" font-size="40" text-anchor="middle" fill="white">MGBX</text>
</svg>
EOF

                    # 创建favicon
                    cp -f "${DOCUSAURUS_DIR}/static/img/logo.svg" "${DOCUSAURUS_DIR}/static/img/favicon.ico"
                '''
            }
        }

        // 初始化Docusaurus
        stage('初始化Docusaurus') {
            steps {
                dir("${DOCUSAURUS_DIR}") {
                    sh '''
                        echo "初始化Docusaurus项目..."
                        npm init -y
                        npm install --save-dev @docusaurus/core @docusaurus/preset-classic
                        npm install --save-dev react react-dom
                    '''
                }
            }
        }

        // 构建文档站
        stage('构建文档站') {
            steps {
                dir("${DOCUSAURUS_DIR}") {
                    echo "开始构建文档站..."
                    timeout(time: 5, unit: 'MINUTES') {
                        sh 'npm install'
                        sh 'npm run build'
                    }
                }
            }
        }

        // 验证文档
        stage('验证文档') {
            steps {
                sh '''
                    echo "验证文档站构建结果..."
                    if [ ! -d "${DOCUSAURUS_DIR}/build" ]; then
                        echo "错误: 构建目录不存在!"
                        exit 1
                    fi

                    # 检查关键文件
                    if [ ! -f "${DOCUSAURUS_DIR}/build/index.html" ]; then
                        echo "错误: index.html 不存在!"
                        exit 1
                    fi

                    # 检查API文档
                    if [ ! -f "${DOCUSAURUS_DIR}/build/api/rest/index.html" ]; then
                        echo "警告: REST API文档页面不存在!"
                    fi

                    if [ ! -f "${DOCUSAURUS_DIR}/build/api/websocket/index.html" ]; then
                        echo "警告: WebSocket API文档页面不存在!"
                    fi

                    # 检查i18n构建结果
                    if [ ! -d "${DOCUSAURUS_DIR}/build/en" ]; then
                        echo "警告: 英文版构建结果不存在!"
                    else
                        echo "英文版构建成功!"
                    fi

                    echo "文档站验证完成"
                '''
            }
        }

        // 部署阶段(可选)
        stage('部署文档') {
            when {
                expression { return params.DEPLOY_TO_PRODUCTION }
            }
            steps {
                echo "开始部署文档..."
                // 部署步骤(根据实际环境配置)

                // 如果需要清除CDN缓存
                script {
                    if (params.INVALIDATE_CACHE) {
                        echo "执行CDN缓存失效操作..."
                        // 这里添加使CDN缓存失效的命令
                    }
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