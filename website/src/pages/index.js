import React from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import Heading from '@theme/Heading';

import styles from './index.module.css';

function HomepageHeader() {
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
        <div className="row align-items--center">
          <div className="col col--6">
            <Heading as="h1" className="hero__title">
              Build Better Habits, One Purr at a Time.
            </Heading>
            <p className="hero__subtitle">
              Stay persistent, achieve your goals, and get rewarded. Earn fishies to adopt virtual cats and build your ultimate cat family.
            </p>
            <div className={styles.buttons}>
              <Link
                className="button button--secondary button--lg"
                to="https://github.com/frvnzz/purrsistence/releases">
                Download the latest Release (APK)
              </Link>
            </div>
          </div>
          <div className="col col--6 text--center">
            <div className={styles.phoneMockup}>
              <img src="./img/Home.png" alt="Purrsistence Home Screen" />
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}

function CustomFeatures() {
  const features = [
    {
      title: 'Daily Habit Tracking',
      description: 'Set custom goals and track your daily routines. Consistency is key to building lasting habits.',
    },
    {
      title: 'Build your Cat Family',
      description: "Earn fishies for every completed goal. Use them to adopt a variety of adorable cats that hang out in your app's room.",
    },
    {
      title: 'Detailed Insights',
      description: 'Visualize your success with clear statistics. View your past performance and time-tracking history.',
    },
  ];

  return (
    <section className="padding-vert--xl">
      <div className="container">
        <div className="text--center margin-bottom--xl">
          <Heading as="h2">Everything you need to stay on track</Heading>
          <p>A perfect blend of productivity and playfulness to keep you motivated.</p>
        </div>
        <div className="row">
          {features.map((props, idx) => (
            <div key={idx} className={clsx('col col--4')}>
              <div className="text--center padding-horiz--md">
                <Heading as="h3">{props.title}</Heading>
                <p>{props.description}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function GamificationSection() {
  return (
    <section className="padding-vert--xl" style={{backgroundColor: 'var(--ifm-color-emphasis-100)'}}>
      <div className="container">
        <div className="row align-items--center">
          <div className="col col--6 margin-bottom--lg">
            <div className="padding-horiz--md">
              <span className="badge badge--primary margin-bottom--md">Gamification</span>
              <Heading as="h2">Reward your persistence with virtual pets.</Heading>
              <p>
                Completing your daily goals shouldn't feel like a chore. With Purrsistence, every completed goal earns you "Fishies".
              </p>
              <p>
                Visit the Shop to use your hard-earned Fishies and unlock cute cats. Watch them and interact with them in your home room whenever you open the app!
              </p>
              <ul>
                <li>Earn currency for staying on track</li>
                <li>Collect rare and unique cats</li>
                <li>A visual representation of your progress</li>
              </ul>
            </div>
          </div>
          <div className="col col--6 text--center">
            <div className={styles.phoneMockup}>
              <img src="./img/Shop.png" alt="Purrsistence Shop Screen" />
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

function StatisticsSection() {
  return (
    <section className="padding-vert--xl">
      <div className="container">
        <div className="row align-items--center">
          <div className="col col--6 text--center margin-bottom--lg">
            <div className={styles.phoneMockup}>
              <img src="./img/Statistics.png" alt="Purrsistence Statistics Screen" />
            </div>
          </div>
          <div className="col col--6">
            <div className="padding-horiz--md">
              <span className="badge badge--primary margin-bottom--md">Insights</span>
              <Heading as="h2">Your progress,<br />visualized.</Heading>
              <p>
                Keep track of your consistency over time. Our statistics overview provides you with insights on your tracked time and historical data.
              </p>
              <p>
                Identify trends and optimize your routine to achieve your goals faster.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

function DownloadSection() {
  return (
    <section className="padding-vert--xl text--center" style={{backgroundColor: 'var(--ifm-color-emphasis-100)'}}>
      <div className="container">
        <Heading as="h2">Ready to build your ultimate cat family?</Heading>
        <p className="margin-bottom--lg">
          Download Purrsistence today and turn your daily and weekly goals into a rewarding adventure.
        </p>
        <div style={{ display: 'flex', justifyContent: 'center' }}>
          <Link
            className="button button--primary button--lg"
            to="https://github.com/frvnzz/purrsistence/releases">
            Download the latest Release (APK)
          </Link>
        </div>
      </div>
    </section>
  );
}

export default function Home() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`Purrsistence - Build Habits, Adopt Cats`}
      description="Build Better Habits, One Purr at a Time.">
      <HomepageHeader />
      <main>
        <CustomFeatures />
        <GamificationSection />
        <StatisticsSection />
        <DownloadSection />
      </main>
    </Layout>
  );
}
