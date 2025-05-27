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

    parameters {
        booleanParam(name: 'DEPLOY_TO_S3', defaultValue: true, description: '是否部署到S3')
        booleanParam(name: 'INVALIDATE_CACHE', defaultValue: false, description: '是否清除CDN缓存(部署后)')
    }

    stages {
        stage('准备环境') {
            steps {
                echo "准备构建环境"
                sh 'node -v && npm -v'

                sh '''
                    rm -rf ${DOCUSAURUS_DIR}
                    mkdir -p ${API_DOCS_DIR}
                    mkdir -p ${DOCUSAURUS_DIR}/src/css
                    mkdir -p ${DOCUSAURUS_DIR}/static/img
                    mkdir -p ${DOCUSAURUS_DIR}/static/icons
                '''

                script {
                    if (fileExists("${WORKSPACE}/static/img")) {
                        sh '''
                            cp -rf ${WORKSPACE}/static/img/* ${DOCUSAURUS_DIR}/static/img/
                            echo "已复制静态图片资源"
                            ls -la ${DOCUSAURUS_DIR}/static/img/
                        '''
                    } else {
                        echo "未找到静态图片目录，跳过复制"
                    }
                }
            }
        }

        stage('查找API文档') {
            steps {
                echo "查找API文档文件..."
                sh '''
                    echo "所有Markdown文件列表:"
                    find ${WORKSPACE} -name "*.md" -type f | sort

                    # 初始化文档路径变量
                    REST_DOC_PATH=""
                    WEBSOCKET_DOC_PATH=""
                    REST_EN_DOC_PATH=""
                    WEBSOCKET_EN_DOC_PATH=""

                    # 复制预先准备好的图片文件
                    if [ -d "${CONFIGS_DIR}/static/img" ]; then
                        mkdir -p ${DOCUSAURUS_DIR}/static/img/
                        cp -rf ${CONFIGS_DIR}/static/img/* ${DOCUSAURUS_DIR}/static/img/
                        echo "已复制预设图片资源"
                    fi

                    # 按优先级查找文档
                    # 1. 首先查找configs目录
                    if [ -f "${WORKSPACE}/${CONFIGS_DIR}/OPENAPI-SPOT-REST.md" ]; then
                        REST_DOC_PATH="${WORKSPACE}/${CONFIGS_DIR}/OPENAPI-SPOT-REST.md"
                        echo "在configs目录找到REST API文档"
                    fi

                    if [ -f "${WORKSPACE}/${CONFIGS_DIR}/OPENAPI-SPOT-WEBSOCKET.md" ]; then
                        WEBSOCKET_DOC_PATH="${WORKSPACE}/${CONFIGS_DIR}/OPENAPI-SPOT-WEBSOCKET.md"
                        echo "在configs目录找到WebSocket API文档"
                    fi

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

                        if [ -z "$REST_EN_DOC_PATH" ] && [ -f "${DIR}/OPENAPI-SPOT-REST-EN.md" ]; then
                            REST_EN_DOC_PATH="${DIR}/OPENAPI-SPOT-REST-EN.md"
                            echo "在 ${DIR} 目录找到英文REST API文档"
                        fi

                        if [ -z "$WEBSOCKET_EN_DOC_PATH" ] && [ -f "${DIR}/OPENAPI-SPOT-WEBSOCKET-EN.md" ]; then
                            WEBSOCKET_EN_DOC_PATH="${DIR}/OPENAPI-SPOT-WEBSOCKET-EN.md"
                            echo "在 ${DIR} 目录找到英文WebSocket API文档"
                        fi
                    done

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

                    # 处理中文REST API文档
                    if [ -n "$REST_DOC_PATH" ] && [ -f "$REST_DOC_PATH" ]; then
                        cp -f "${REST_DOC_PATH}" "${API_DOCS_DIR}/rest.md"
                        echo "已复制REST API文档: ${REST_DOC_PATH} -> ${API_DOCS_DIR}/rest.md"
                        # 添加前置元数据
                        sed -i '1i ---\\nid: rest\\nslug: api/rest\\ntitle: REST API\\ndescription: MGBX REST API接入文档\\n---\\n' "${API_DOCS_DIR}/rest.md"
                        echo "已添加REST文档前置元数据"
                    else
                        echo "警告: 未找到REST API文档，创建空文档"
                        echo -e "---\\nid: rest\\nslug: api/rest\\ntitle: REST API\\ndescription: MGBX REST API接入文档\\n---\\n\\n# REST API\\n\\n文档正在更新中..." > "${API_DOCS_DIR}/rest.md"
                    fi

                    # 处理中文WebSocket API文档
                    if [ -n "$WEBSOCKET_DOC_PATH" ] && [ -f "$WEBSOCKET_DOC_PATH" ]; then
                        cp -f "${WEBSOCKET_DOC_PATH}" "${API_DOCS_DIR}/websocket.md"
                        echo "已复制WebSocket API文档: ${WEBSOCKET_DOC_PATH} -> ${API_DOCS_DIR}/websocket.md"
                        # 添加前置元数据
                        sed -i '1i ---\\nid: websocket\\nslug: api/websocket\\ntitle: WebSocket API\\ndescription: MGBX WebSocket API接入文档\\n---\\n' "${API_DOCS_DIR}/websocket.md"
                        echo "已添加WebSocket文档前置元数据"
                    else
                        echo "警告: 未找到WebSocket API文档，创建空文档"
                        echo -e "---\\nid: websocket\\nslug: api/websocket\\ntitle: WebSocket API\\ndescription: MGBX WebSocket API接入文档\\n---\\n\\n# WebSocket API\\n\\n文档正在更新中..." > "${API_DOCS_DIR}/websocket.md"
                    fi

                    # 创建中文首页文档
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
                    mkdir -p ${DOCUSAURUS_DIR}/i18n/en/docusaurus-theme-classic

                    # 处理英文REST API文档
                    if [ -n "$REST_EN_DOC_PATH" ] && [ -f "$REST_EN_DOC_PATH" ]; then
                        cp -f "${REST_EN_DOC_PATH}" "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api/rest.md"
                        echo "已复制英文REST API文档: ${REST_EN_DOC_PATH}"
                        # 添加前置元数据（不以斜杠开头的slug）
                        sed -i '1i ---\\nid: rest\\nslug: api/rest\\ntitle: REST API\\ndescription: MGBX REST API Documentation\\n---\\n' "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api/rest.md"
                        echo "已添加英文REST文档前置元数据"
                    else
                        echo "警告: 未找到英文REST API文档，创建空文档"
                        mkdir -p "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api"
                        echo -e "---\\nid: rest\\nslug: api/rest\\ntitle: REST API\\ndescription: MGBX REST API Documentation\\n---\\n\\n# REST API\\n\\nDocumentation is being updated..." > "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api/rest.md"
                    fi

                    # 处理英文WebSocket API文档 - 修复slug格式
                    if [ -n "$WEBSOCKET_EN_DOC_PATH" ] && [ -f "$WEBSOCKET_EN_DOC_PATH" ]; then
                        cp -f "${WEBSOCKET_EN_DOC_PATH}" "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api/websocket.md"
                        echo "已复制英文WebSocket API文档: ${WEBSOCKET_EN_DOC_PATH}"
                        # 添加前置元数据 - 不以斜杠开头的slug
                        sed -i '1i ---\\nid: websocket\\nslug: api/websocket\\ntitle: WebSocket API\\ndescription: MGBX WebSocket API Documentation\\n---\\n' "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api/websocket.md"
                        echo "已添加英文WebSocket文档前置元数据（修正slug格式）"
                    else
                        echo "警告: 未找到英文WebSocket API文档，创建空文档"
                        mkdir -p "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api"
                        echo -e "---\\nid: websocket\\nslug: api/websocket\\ntitle: WebSocket API\\ndescription: MGBX WebSocket API Documentation\\n---\\n\\n# WebSocket API\\n\\nDocumentation is being updated..." > "${DOCUSAURUS_DIR}/i18n/en/docusaurus-plugin-content-docs/current/api/websocket.md"
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

                    # 创建英文侧边栏翻译文件
                    echo "创建英文侧边栏翻译..."
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

                    # 创建英文导航翻译
                    echo "创建英文导航翻译..."
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

                    # 创建英文页脚翻译
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

                // 创建简单logo
                sh '''
                    echo "设置网站图标..."

                    # 检查是否存在原始图片文件
                    if [ -f "${CONFIGS_DIR}/static/img/logo.png" ]; then
                        # 使用原始的 PNG logo
                        cp -f "${CONFIGS_DIR}/static/img/logo.png" "${DOCUSAURUS_DIR}/static/img/logo.png"
                        echo "已使用原始 logo.png 文件"
                    else
                        echo "警告: 未找到原始 logo.png 文件"
                    fi

                    # 检查并使用原始 favicon
                    if [ -f "${CONFIGS_DIR}/static/img/favicon.ico" ]; then
                        cp -f "${CONFIGS_DIR}/static/img/favicon.ico" "${DOCUSAURUS_DIR}/static/img/favicon.ico"
                        echo "已使用原始 favicon.ico 文件"
                    elif [ -f "${CONFIGS_DIR}/static/img/logo.png" ]; then
                        # 如果没有 favicon 但有 logo，使用 logo 作为 favicon
                        cp -f "${CONFIGS_DIR}/static/img/logo.png" "${DOCUSAURUS_DIR}/static/img/favicon.ico"
                        echo "已将原始 logo.png 复制为 favicon.ico"
                    else
                        echo "警告: 未找到原始 favicon.ico 文件"
                    fi

                    echo "图片文件信息:"
                    ls -la ${DOCUSAURUS_DIR}/static/img/
                '''
            }
        }

        stage('初始化Docusaurus') {
            steps {
                dir("${DOCUSAURUS_DIR}") {
                    sh '''
                        echo "初始化Docusaurus项目..."
                        # 创建稳定的package.json - 避免版本不兼容问题
                        cat > package.json << EOF
{
  "name": "mgbx-api-docs",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "docusaurus": "docusaurus",
    "start": "docusaurus start",
    "build": "docusaurus build",
    "swizzle": "docusaurus swizzle",
    "deploy": "docusaurus deploy",
    "clear": "docusaurus clear",
    "serve": "docusaurus serve",
    "write-translations": "docusaurus write-translations",
    "write-heading-ids": "docusaurus write-heading-ids"
  },
  "dependencies": {
    "@docusaurus/core": "2.4.3",
    "@docusaurus/preset-classic": "2.4.3",
    "@mdx-js/react": "^1.6.22",
    "clsx": "^1.2.1",
    "react": "^17.0.2",
    "react-dom": "^17.0.2"
  },
  "devDependencies": {
    "@docusaurus/module-type-aliases": "2.4.3"
  },
  "browserslist": {
    "production": [
      ">0.5%",
      "not dead",
      "not op_mini all"
    ],
    "development": [
      "last 1 chrome version",
      "last 1 firefox version",
      "last 1 safari version"
    ]
  },
  "engines": {
    "node": ">=16.14"
  }
}
EOF
                        echo "已创建package.json文件"

                        # 安装依赖
                        echo "安装Docusaurus依赖..."
                        npm install
                    '''
                }
            }
        }

        stage('构建文档站') {
            steps {
                dir("${DOCUSAURUS_DIR}") {
                    echo "开始构建文档站..."
                    timeout(time: 5, unit: 'MINUTES') {
                        sh 'npm run build'
                    }
                }
            }
        }

        stage('验证文档') {
            steps {
                sh '''
                    echo "验证文档站构建结果..."
                    if [ ! -d "${DOCUSAURUS_DIR}/build" ]; then
                        echo "错误: 构建目录不存在!"
                        exit 1
                    fi

                    # 检查核心文件
                    if [ ! -f "${DOCUSAURUS_DIR}/build/index.html" ]; then
                        echo "错误: 首页(index.html)不存在!"
                        exit 1
                    fi

                    # 检查中文API文档
                    echo "检查中文API文档页面..."
                    if [ ! -f "${DOCUSAURUS_DIR}/build/api/rest/index.html" ]; then
                        echo "警告: 中文REST API页面不存在!"
                    else
                        echo "中文REST API页面生成成功"
                    fi

                    if [ ! -f "${DOCUSAURUS_DIR}/build/api/websocket/index.html" ]; then
                        echo "警告: 中文WebSocket API页面不存在!"
                    else
                        echo "中文WebSocket API页面生成成功"
                    fi

                    # 检查英文版构建结果
                    echo "检查英文版页面..."
                    if [ ! -d "${DOCUSAURUS_DIR}/build/en" ]; then
                        echo "警告: 英文版目录不存在!"
                    else
                        echo "英文版目录生成成功"

                        # 检查英文API文档
                        if [ ! -f "${DOCUSAURUS_DIR}/build/en/api/rest/index.html" ]; then
                            echo "警告: 英文REST API页面不存在!"
                        else
                            echo "英文REST API页面生成成功"
                        fi

                        if [ ! -f "${DOCUSAURUS_DIR}/build/en/api/websocket/index.html" ]; then
                            echo "警告: 英文WebSocket API页面不存在! 查找可能的位置:"
                            find "${DOCUSAURUS_DIR}/build/en" -type f -name "*.html" | grep -i websocket || echo "未找到相关页面"
                        else
                            echo "英文WebSocket API页面生成成功"
                        fi
                    fi

                    # 输出构建目录结构以便调试
                    echo "构建目录结构:"
                    find "${DOCUSAURUS_DIR}/build" -type d | sort | head -n 20

                    echo "文档站验证完成"
                '''
            }
        }

        stage('部署到S3') {
            when {
                expression { return params.DEPLOY_TO_S3 }
            }
            steps {
                withAWS(region: 'ap-northeast-1', credentials: 'aws-credentials') {
                    sh '''
                        echo "开始部署到S3..."
                        aws s3 sync ${DOCUSAURUS_DIR}/build/ s3://web1156 --delete

                        # 配置S3静态网站托管和404错误页面(跳转到首页)
                        echo "配置S3静态网站错误页面跳转到首页..."
                        aws s3 website s3://web1156 --index-document index.html --error-document index.html

                        echo "部署完成: http://web1156.s3-website-ap-northeast-1.amazonaws.com"

                        # 刷新CloudFront缓存
                        if [ "${INVALIDATE_CACHE}" = "true" ]; then
                            echo "正在刷新CDN缓存..."
                            # aws cloudfront create-invalidation --distribution-id YOUR_DISTRIBUTION_ID --paths "/*"
                            echo "缓存刷新请求已发送"
                        fi
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "构建成功: 文档站已生成并部署"
        }
        failure {
            echo "构建失败: 请检查日志"
        }
        always {
            sh "rm -f rest_doc_path.txt websocket_doc_path.txt rest_en_doc_path.txt websocket_en_doc_path.txt || true"
        }
    }
}