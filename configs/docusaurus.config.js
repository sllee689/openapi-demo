const config = {
    title: 'MGBX API 文档中心',
    tagline: '接入 MGBX 交易平台 API',
    favicon: 'img/favicon.ico',
    url: 'https://apidocs.mgbx.com',
    baseUrl: '/',
    onBrokenLinks: 'warn',
    onBrokenMarkdownLinks: 'warn',
    // 修改为 true，强制添加尾部斜杠
    trailingSlash: true,

    // 添加构建时配置，生成 .html 后缀
    staticDirectories: ['static'],

    // 启用 noIndex 减少干扰
    noIndex: false,

    i18n: {
        defaultLocale: 'zh',
        locales: ['zh', 'en'],
        localeConfigs: {
            zh: {
                label: '中文',
                direction: 'ltr',
            },
            en: {
                label: 'English',
                direction: 'ltr',
            },
        },
    },

    presets: [
        [
            'classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    routeBasePath: '/', // 文档作为主页
                    sidebarPath: require.resolve('./sidebars.js'),
                },
                blog: false, // 禁用博客功能
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            }),
        ],
    ],

    // 添加自定义插件处理 URL 格式
    plugins: [
        function htmlSuffixPlugin() {
            return {
                name: 'html-suffix-plugin',
                configureWebpack() {
                    return {};
                },
                // 添加客户端代码修复路径问题
                injectHtmlTags() {
                    return {
                        headTags: [
                            {
                                tagName: 'script',
                                innerHTML: `
                                  // 修复语言切换 URL 问题
                                  document.addEventListener('DOMContentLoaded', function() {
                                    // 获取所有语言切换链接
                                    const localeLinks = document.querySelectorAll('.navbar__item.dropdown__link');
                                    localeLinks.forEach(link => {
                                      link.addEventListener('click', function(e) {
                                        const href = this.getAttribute('href');
                                        if (href && !href.endsWith('/') && !href.includes('.html')) {
                                          // 确保添加 .html 后缀或尾部斜杠
                                          e.preventDefault();
                                          window.location.href = href + '/';
                                        }
                                      });
                                    });
                                  });
                                `
                            }
                        ]
                    };
                }
            };
        }
    ],

    themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
        ({
            // 禁用搜索功能，移除多余依赖
            algolia: undefined,

            // 简化导航栏
            navbar: {
                title: 'MGBX API 文档',
                logo: {
                    alt: 'MGBX',
                    src: 'img/logo.png',
                },
                items: [
                    {
                        href: 'https://github.com/megabit-open/openapi-spot-docs',
                        label: 'GitHub',
                        position: 'right',
                        className: 'github-link',
                    },
                    {
                        href: 'https://www.mgbx.com',
                        label: '官网',
                        position: 'right',
                    },
                    {
                        type: 'localeDropdown',
                        position: 'right',
                    },
                ],
            },

            // 移除页脚链接
            footer: {
                style: 'dark',
                links: [
                ],
                copyright: `Copyright © ${new Date().getFullYear()} MGBX. All rights reserved.`,
            },

            // 无标题链接跳转
            docs: {
                sidebar: {
                    hideable: false,
                    autoCollapseCategories: false,
                },
            },

            // 优化主题配置
            colorMode: {
                defaultMode: 'light',
                disableSwitch: false,
                respectPrefersColorScheme: true,
            },
        }),
};

module.exports = config;