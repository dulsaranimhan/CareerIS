import { useEffect, useState } from 'react';

const internships = [
  { company: 'Northstar Studio', mark: 'N', tone: 'lavender', role: 'Junior UI/UX Designer', detail: 'Colombo · Hybrid', tag: 'Design', days: '2 days ago' },
  { company: 'Greenline Labs', mark: 'G', tone: 'mint', role: 'Software Engineering Intern', detail: 'Remote · Sri Lanka', tag: 'Engineering', days: '4 days ago' },
  { company: 'Common Ground', mark: 'C', tone: 'peach', role: 'Marketing Intern', detail: 'Colombo · On-site', tag: 'Marketing', days: '1 week ago' },
];

function ArrowUpRight() {
  return <span aria-hidden="true" className="arrow-up-right">↗</span>;
}

function authRouteFromHash() {
  const match = window.location.hash.match(/^#(register|sign-in)(?:\/(student|company))?$/);
  return match ? { mode: match[1], role: match[2] || 'student' } : null;
}

function AuthScreen({ mode, role, onModeChange, onRoleChange, onHome }) {
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const isRegister = mode === 'register';
  const isCompany = role === 'company';

  async function handleSubmit(event) {
    event.preventDefault();
    const form = event.currentTarget;
    setMessage('');
    const formData = new FormData(form);
    if (isRegister && formData.get('password') !== formData.get('confirmPassword')) {
      setMessage('The passwords do not match. Please check them and try again.');
      setMessageType('error');
      return;
    }
    if (!isRegister) {
      setMessage('Sign-in is not connected yet. You can create an account now; sign-in is the next backend feature.');
      setMessageType('info');
      return;
    }

    setSubmitting(true);
    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          role: role.toUpperCase(),
          name: formData.get(isCompany ? 'companyName' : 'fullName'),
          contactName: isCompany ? formData.get('contactName') : undefined,
          email: formData.get('email'),
          password: formData.get('password'),
        }),
      });
      const result = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(result.message || 'We could not create your account. Please try again.');
      }
      setMessage(`Account created for ${result.email}. Sign-in will be available in the next step.`);
      setMessageType('success');
      form.reset();
    } catch (error) {
      setMessage(error.message || 'Could not reach CareerIS. Check that the backend and MongoDB are running.');
      setMessageType('error');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="auth-shell">
      <div className="auth-art" aria-hidden="true"><span className="auth-orbit auth-orbit-one" /><span className="auth-orbit auth-orbit-two" /><span className="auth-art-star">✳</span><div className="auth-art-note"><span className="auth-art-note-icon">↗</span><span><b>A new chapter</b><small>begins with one step</small></span></div><div className="auth-art-message"><span>CareerIS</span><strong>Good beginnings<br />start here.</strong><small>Students × Companies × Opportunity</small></div></div>
      <section className="auth-panel" aria-labelledby="auth-title">
        <a className="brand auth-brand" href="#top" onClick={(event) => { event.preventDefault(); onHome(); }}><span className="brand-symbol"><span /></span><span>Career<span className="brand-is">IS</span></span></a>
        <div className="auth-content">
          <a className="auth-back" href="#top" onClick={(event) => { event.preventDefault(); onHome(); }}>← <span>Back to home</span></a>
          <p className="eyebrow">{isRegister ? 'START YOUR NEXT CHAPTER' : 'WELCOME BACK'}</p>
          <h1 id="auth-title">{isRegister ? 'Create your account' : 'Good to see you again.'}</h1>
          <p className="auth-intro">{isRegister ? 'Join CareerIS and take the next step in your journey.' : 'Sign in to continue your CareerIS journey.'}</p>

          <div className="account-type" role="group" aria-label="Choose account type">
            <button type="button" className={!isCompany ? 'selected' : ''} aria-pressed={!isCompany} onClick={() => onRoleChange('student')}><span>♧</span> Student</button>
            <button type="button" className={isCompany ? 'selected' : ''} aria-pressed={isCompany} onClick={() => onRoleChange('company')}><span>▤</span> Company</button>
          </div>

          <form className="auth-form" onSubmit={handleSubmit}>
            {isRegister && !isCompany && <label>Full name<input name="fullName" type="text" autoComplete="name" placeholder="e.g. Maya Perera" required /></label>}
            {isRegister && isCompany && <><label>Company name<input name="companyName" type="text" autoComplete="organization" placeholder="e.g. Northstar Studio" required /></label><label>Your name<input name="contactName" type="text" autoComplete="name" placeholder="e.g. Alex Fernando" required /></label></>}
            <label>{isCompany ? 'Work email' : 'Email address'}<input name="email" type="email" autoComplete="email" placeholder={isCompany ? 'you@company.com' : 'you@example.com'} required /></label>
            <label>Password<input name="password" type="password" autoComplete={isRegister ? 'new-password' : 'current-password'} placeholder="At least 8 characters" minLength={8} required /></label>
            {isRegister && <label>Confirm password<input name="confirmPassword" type="password" autoComplete="new-password" placeholder="Enter your password again" minLength={8} required /></label>}
            {!isRegister && <a className="forgot-link" href="#forgot-password" onClick={(event) => { event.preventDefault(); setMessage('Password recovery will be available when account services are connected.'); }}>Forgot password?</a>}
            <button className="button button-purple auth-submit" type="submit" disabled={submitting}>{submitting ? 'Creating account…' : isRegister ? 'Create account' : 'Sign in'} <ArrowUpRight /></button>
            {message && <p className={`form-message ${messageType}`} role="status">{message}</p>}
          </form>
          <p className="auth-switch">{isRegister ? 'Already have an account?' : 'New to CareerIS?'} <button type="button" onClick={() => onModeChange(isRegister ? 'sign-in' : 'register')}>{isRegister ? 'Sign in' : 'Create an account'}</button></p>
          <p className="auth-note">Your password is protected before it is saved. CareerIS will never show it in your profile.</p>
        </div>
        <footer className="auth-footer"><span>© 2026 CareerIS</span><span>Good beginnings, built together.</span></footer>
      </section>
    </main>
  );
}

function App() {
  const [auth, setAuth] = useState(authRouteFromHash);

  useEffect(() => {
    const syncRoute = () => setAuth(authRouteFromHash());
    window.addEventListener('hashchange', syncRoute);
    window.addEventListener('popstate', syncRoute);
    return () => {
      window.removeEventListener('hashchange', syncRoute);
      window.removeEventListener('popstate', syncRoute);
    };
  }, []);

  function openAuth(mode, role = 'student') {
    const next = { mode, role };
    window.history.pushState(next, '', `#${mode}/${role}`);
    setAuth(next);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  if (auth) {
    return <AuthScreen mode={auth.mode} role={auth.role} onModeChange={(mode) => openAuth(mode, auth.role)} onRoleChange={(role) => openAuth(auth.mode, role)} onHome={() => { window.history.pushState(null, '', window.location.pathname); setAuth(null); }} />;
  }

  return (
    <main className="site-shell" id="top">
      <header className="topbar">
        <a className="brand" href="#top" aria-label="CareerIS home">
          <span className="brand-symbol"><span /></span><span>Career<span className="brand-is">IS</span></span>
        </a>
        <nav className="main-nav" aria-label="Main navigation">
          <a className="active" href="#opportunities">Find internships</a>
          <a href="#for-companies">For companies</a>
          <a href="#how-it-works">How it works</a>
        </nav>
        <div className="nav-actions"><a className="sign-in" href="#sign-in/student" onClick={(event) => { event.preventDefault(); openAuth('sign-in'); }}>Sign in</a><a className="button button-dark nav-join" href="#register/student" onClick={(event) => { event.preventDefault(); openAuth('register'); }}>Join CareerIS <ArrowUpRight /></a></div>
        <a className="mobile-join" href="#register/student" onClick={(event) => { event.preventDefault(); openAuth('register'); }}>Join <ArrowUpRight /></a>
      </header>

      <section className="hero" aria-labelledby="hero-title">
        <div className="hero-grid-pattern" />
        <div className="hero-glow glow-left" /><div className="hero-glow glow-right" />
        <div className="hero-inner">
          <div className="hero-copy">
            <p className="eyebrow"><span className="status-dot" /> YOUR NEXT CHAPTER STARTS HERE</p>
            <h1 id="hero-title">Find your place.<br /><span>Make your start.</span></h1>
            <p className="hero-description">Discover internships that fit your ambitions. Meet companies ready to give new talent a chance.</p>
            <div className="hero-ctas"><a className="button button-purple" href="#opportunities">Explore internships <ArrowUpRight /></a><a className="button button-light" href="#register/company" onClick={(event) => { event.preventDefault(); openAuth('register', 'company'); }}>I’m hiring <ArrowUpRight /></a></div>
            <div className="hero-proof"><div className="proof-avatars"><i>R</i><i>S</i><i>M</i><i>+</i></div><p><strong>A good first step changes everything.</strong><br />Made for students and the people who hire them.</p></div>
          </div>

          <div className="hero-visual" aria-label="CareerIS internship dashboard preview" role="img">
            <div className="visual-orbit orbit-a" /><div className="visual-orbit orbit-b" />
            <div className="float-card float-new"><span className="float-icon purple-icon">✳</span><span><b>Fresh opportunities</b><small>New internships this week</small></span><span className="float-count">12</span></div>
            <div className="float-card float-match"><span className="match-check">✓</span><span><b>Application sent</b><small>Your next step is underway</small></span></div>
            <div className="visual-spark spark-left">✳</div><div className="visual-spark spark-right">✦</div>
            <div className="preview-window">
              <div className="preview-topline"><div className="preview-brand"><span className="mini-brand-mark">C</span> CareerIS</div><span className="preview-greeting">STUDENT DASHBOARD</span><span className="preview-avatar">M</span></div>
              <div className="preview-content">
                <div className="preview-welcome"><div><span className="preview-kicker">TUESDAY, OCTOBER 15</span><h3>Your journey, at a glance.</h3></div><span className="welcome-spark">✳</span></div>
                <div className="preview-stats"><div className="preview-stat"><span className="stat-icon stat-purple">▤</span><span className="stat-label">Applications</span><b>08</b><small>Keep the momentum</small></div><div className="preview-stat"><span className="stat-icon stat-orange">◷</span><span className="stat-label">Interviews</span><b>02</b><small>Good things ahead</small></div><div className="preview-stat"><span className="stat-icon stat-green">✦</span><span className="stat-label">Shortlisted</span><b>03</b><small>You’re on their radar</small></div></div>
                <div className="preview-bottom"><div className="preview-activity"><div className="preview-section-title"><b>Application activity</b><span>Last 7 days⌄</span></div><div className="activity-chart"><i style={{ height: '36%' }} /><i style={{ height: '54%' }} /><i style={{ height: '44%' }} /><i style={{ height: '78%' }} /><i style={{ height: '61%' }} /><i style={{ height: '92%' }} /><i style={{ height: '69%' }} /></div><div className="chart-days"><span>MON</span><span>TUE</span><span>WED</span><span>THU</span><span>FRI</span><span>SAT</span><span>SUN</span></div></div><div className="preview-next"><span className="next-label">UP NEXT</span><div className="next-company"><span className="next-logo">N</span><span><b>Northstar Studio</b><small>UI/UX Design Intern</small></span></div><div className="next-status"><span /> Application under review <ArrowUpRight /></div></div></div>
              </div>
            </div>
          </div>
        </div>
        <div className="hero-bottom"><span>TAKE YOUR FIRST STEP WITH</span><div className="trust-line"><i>✳</i> Students <b>×</b> Companies <b>×</b> Opportunity</div><a href="#how-it-works">See how it works <span>↓</span></a></div>
      </section>

      <section className="opportunities section-wrap" id="opportunities">
        <div className="section-heading"><div><p className="eyebrow">A START WORTH FINDING</p><h2>Open doors, <span>real experience.</span></h2></div><a className="text-link" href="#register/student" onClick={(event) => { event.preventDefault(); openAuth('register'); }}>Browse all internships <ArrowUpRight /></a></div>
        <div className="internship-grid">{internships.map((item) => <article className="internship-card" key={item.company}><div className="listing-top"><span className={`company-mark ${item.tone}`}>{item.mark}</span><span className="listing-age">{item.days}</span></div><p className="company-name">{item.company}</p><h3>{item.role}</h3><p className="listing-detail">{item.detail}</p><div className="listing-footer"><span className="category-pill">{item.tag}</span><a href="#register/student" onClick={(event) => { event.preventDefault(); openAuth('register'); }} aria-label={`View ${item.role}`}><ArrowUpRight /></a></div></article>)}</div>
      </section>

      <section className="split-section section-wrap" id="how-it-works">
        <div className="split-card student-card"><div className="split-icon">✳</div><p className="eyebrow">FOR STUDENTS</p><h2>Your first role<br />is a big deal.</h2><p>Show companies what you bring. Add your qualifications, share your CV, and apply to opportunities that interest you.</p><a className="text-link" href="#register/student" onClick={(event) => { event.preventDefault(); openAuth('register'); }}>Build your profile <ArrowUpRight /></a><div className="split-decoration student-deco">01<span>→</span>02</div></div>
        <div className="split-card company-card" id="for-companies"><div className="split-icon company-icon">↗</div><p className="eyebrow">FOR COMPANIES</p><h2>Meet the talent<br />ready to begin.</h2><p>Share an internship vacancy, review applications, and invite promising candidates to interview.</p><a className="text-link" href="#register/company" onClick={(event) => { event.preventDefault(); openAuth('register', 'company'); }}>Post a vacancy <ArrowUpRight /></a><div className="company-stamp">CAREER<br />STARTS HERE</div></div>
      </section>

      <section className="closing-cta" id="join"><div className="cta-decoration">✳</div><p className="eyebrow">THE NEXT STEP IS YOURS</p><h2>Let’s make it <span>happen.</span></h2><p>Whether you’re looking for a start or someone ready to grow, there’s a place for you here.</p><div className="hero-ctas"><a className="button button-dark" href="#register/student" onClick={(event) => { event.preventDefault(); openAuth('register'); }}>Join as a student <ArrowUpRight /></a><a className="button button-light" href="#register/company" onClick={(event) => { event.preventDefault(); openAuth('register', 'company'); }}>Join as a company <ArrowUpRight /></a></div></section>

      <footer className="site-footer"><a className="brand" href="#top"><span className="brand-symbol"><span /></span><span>Career<span className="brand-is">IS</span></span></a><span>Good beginnings, built together.</span><div><a href="#opportunities">Internships</a><a href="#for-companies">Companies</a><a href="#how-it-works">About</a></div><small>© 2026 CareerIS</small></footer>
    </main>
  );
}

export default App;
