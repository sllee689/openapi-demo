/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
    apiSidebar: [
        {
            type: 'doc',
            id: 'intro',
            label: '概述',
        },
        {
            type: 'category',
            label: '现货',
            collapsible: true,
            collapsed: false,
            items: [
                {
                    type: 'category',
                    label: 'REST API',
                    link: {
                        type: 'doc',
                        id: 'api/rest',
                    },
                    items: [
                        'api/rest',
                    ],
                },
                {
                    type: 'category',
                    label: 'WebSocket API',
                    link: {
                        type: 'doc',
                        id: 'api/websocket',
                    },
                    items: [
                        'api/websocket',
                    ],
                },
            ],
        },
    ],
};

module.exports = sidebars;