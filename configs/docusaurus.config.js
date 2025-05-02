/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'MGBX API 文档中心',
    tagline: '接入 MGBX 交易平台 API',
    favicon: 'img/favicon.ico',
    url: 'https://api-docs.mgbx.com',
    baseUrl: '/',
    onBrokenLinks: 'throw', // 严格检查链接错误
    onBrokenMarkdownLinks: 'throw',
    trailingSlash: false,

    // 仅支持中文
    i18n: {
        defaultLocale: 'zh-Hans',
        locales: ['zh-Hans'],
    },

    presets: [
        [
            'classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    routeBasePath: '/', // 文档作为主页
                    sidebarPath: require.resolve('./sidebars.js'),
                    editUrl: null, // 移除编辑链接
                },
                blog: false, // 禁用博客功能
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            }),
        ],
    ],

    // 禁用自动生成的页面以减少多余链接
    plugins: [],

    themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
        ({
            // 禁用搜索功能，移除多余依赖
            algolia: undefined,

            // 简化导航栏
            navbar: {
                title: 'MGBX API 文档',
                logo: {
                    alt: 'MGBX Logo',
                    src: 'img/logo.svg',
                },
                items: [
                    {
                        href: 'https://www.mgbx.com',
                        label: '官网',
                        position: 'right',
                    },
                ],
            },

            // 移除页脚链接
            footer: {
                style: 'dark',
                links: [], // 不显示任何链接
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