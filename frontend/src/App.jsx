const steps = [
  { number: '01', title: 'Build your profile', description: 'Add your education, qualifications, skills, and CV.' },
  { number: '02', title: 'Explore vacancies', description: 'Browse internship opportunities published by companies.' },
  { number: '03', title: 'Apply and follow up', description: 'Apply to a vacancy and keep track of your application status.' },
];

function App() {
  return (
    <main className="page-shell">
      <nav className="nav-bar" aria-label="Main navigation">
        <a className="brand" href="#top"><span className="brand-mark">C</span> CareerIS</a>
        <div className="nav-links"><a href="#how-it-works">How it works</a><a className="nav-login" href="#get-started">Sign in</a></div>
      </nav>

      <section className="hero" id="top">
        <div className="hero-copy">
          <p className="eyebrow"><span className="eyebrow-dot" /> STUDENTS & COMPANIES</p>
          <h1>Your next step<br />starts <span>here.</span></h1>
          <p className="hero-description">A simple place for students to find internship opportunities and for companies to meet emerging talent.</p>
          <div className="hero-actions" id="get-started">
            <a className="button button-primary" href="#how-it-works">Explore opportunities <span aria-hidden="true">↗</span></a>
            <a className="button button-secondary" href="#how-it-works">Post a vacancy</a>
          </div>
          <p className="hero-note">Create a profile. Find a fit. Take the next step.</p>
        </div>
        <div className="hero-art" aria-label="Career journey illustration" role="img">
          <div className="art-orbit orbit-one" /><div className="art-orbit orbit-two" />
          <div className="art-card card-top"><span className="card-icon">✳</span><span><b>New opportunities</b><small>Ready for your next step</small></span></div>
          <div className="art-center"><div className="sun">✦</div><div className="path path-a" /><div className="path path-b" /><div className="path path-c" /><div className="path-dot dot-a" /><div className="path-dot dot-b" /><div className="path-dot dot-c" /><span className="art-caption">A path that fits you</span></div>
          <div className="art-card card-bottom"><span className="avatar-stack"><i>J</i><i>M</i><i>A</i></span><span><b>Built for beginnings</b><small>Students × companies</small></span></div>
          <span className="art-spark spark-one">✳</span><span className="art-spark spark-two">✦</span>
        </div>
      </section>

      <section className="how-section" id="how-it-works">
        <div className="section-heading"><p className="eyebrow">A CLEAR WAY FORWARD</p><h2>From ready to <span>rising.</span></h2></div>
        <div className="steps-grid">{steps.map((step) => <article className="step-card" key={step.number}><span className="step-number">{step.number}</span><h3>{step.title}</h3><p>{step.description}</p></article>)}</div>
      </section>
      <footer><a className="brand" href="#top"><span className="brand-mark">C</span> CareerIS</a><span>Making the first step a little clearer.</span><span>© CareerIS</span></footer>
    </main>
  );
}

export default App;
