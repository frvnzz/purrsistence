// @ts-check
// `@type` JSDoc annotations allow editor autocompletion and type checking
// (when paired with `@ts-check`).
// There are various equivalent ways to declare your Docusaurus config.
// See: https://docusaurus.io/docs/api/docusaurus-config

import { themes as prismThemes } from 'prism-react-renderer';

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Purrsistence',
  tagline: 'Build habits. Collect cats.',
  favicon: './img/favicon.ico',

  future: {
    v4: true,
  },

  url: 'https://frvnzz.github.io',
  baseUrl: '/purrsistence/',

  organizationName: 'frvnzz',
  projectName: 'purrsistence',

  onBrokenLinks: 'throw',

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: './sidebars.js',
          //editUrl: 'https://github.com/frvnzz/purrsistence/tree/main/',
        },
        blog: {
          showReadingTime: true,
          feedOptions: {
            type: ['rss', 'atom'],
            xslt: true,
          },
          //editUrl: 'https://github.com/frvnzz/purrsistence/tree/main/',
          onInlineTags: 'warn',
          onInlineAuthors: 'warn',
          onUntruncatedBlogPosts: 'warn',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      image: 'img/docusaurus-social-card.jpg',
      colorMode: {
        respectPrefersColorScheme: true,
      },
      navbar: {
        title: 'Purrsistence',
        logo: {
          alt: 'Purrsistence Logo',
          src: 'img/logo.svg',
          width: 55,
          height: 55,
        },
        items: [
          {
            type: 'docSidebar',
            sidebarId: 'tutorialSidebar',
            position: 'left',
            label: 'Docs',
          },
          {
            to: '/app/',
            label: 'App',
            position: 'left',
          },
          {
            to: '/download/',
            label: 'Download',
            position: 'left',
          },
          {
            to: '/team/',
            label: 'Team',
            position: 'left',
          },
          {
            href: 'https://github.com/frvnzz/purrsistence',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'More',
            items: [
              {
                label: 'GitHub',
                href: 'https://github.com/frvnzz/purrsistence',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Purrsistence.`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
      },
    }),
};

export default config;