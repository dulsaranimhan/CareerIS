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
  const match = window.location.hash.match(/^#(register|sign-in|dashboard)(?:\/(student|company))?$/);
  return match ? { mode: match[1], role: match[2] || 'student' } : null;
}

function AuthScreen({ mode, role, onModeChange, onRoleChange, onHome, onLogin }) {
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
    setSubmitting(true);
    try {
      const payload = isRegister
        ? {
            role: role.toUpperCase(),
            name: formData.get(isCompany ? 'companyName' : 'fullName'),
            contactName: isCompany ? formData.get('contactName') : undefined,
            email: formData.get('email'),
            password: formData.get('password'),
          }
        : {
            role: role.toUpperCase(),
            email: formData.get('email'),
            password: formData.get('password'),
          };
      const response = await fetch(isRegister ? '/api/auth/register' : '/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      const result = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(result.message || 'We could not create your account. Please try again.');
      }
      if (!isRegister) {
        onLogin(result);
        return;
      }
      setMessage(`Account created for ${result.email}. You can sign in now.`);
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
            <button className="button button-purple auth-submit" type="submit" disabled={submitting}>{submitting ? isRegister ? 'Creating account…' : 'Signing in…' : isRegister ? 'Create account' : 'Sign in'} <ArrowUpRight /></button>
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

const emptyVacancy = () => ({ title: '', description: '', skills: '', location: '', workMode: 'HYBRID', deadline: '' });

async function vacancyApi(path, token, options = {}) {
  const response = await fetch(path, {
    ...options,
    headers: {
      Authorization: `Bearer ${token}`,
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers,
    },
  });
  const result = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(result.message || 'Could not complete that request. Please try again.');
  return result;
}

function StudentWorkspace({ session }) {
  const [vacancies, setVacancies] = useState([]);
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [search, setSearch] = useState('');
  const [workMode, setWorkMode] = useState('ALL');
  const [view, setView] = useState('vacancies');
  const [selectedVacancy, setSelectedVacancy] = useState(null);
  const [coverLetter, setCoverLetter] = useState('');
  const [applying, setApplying] = useState(false);

  async function loadStudentData() {
    setLoading(true);
    setError('');
    try {
      const [openVacancies, myApplications] = await Promise.all([
        vacancyApi('/api/vacancies', session.accessToken),
        vacancyApi('/api/student/applications', session.accessToken),
      ]);
      setVacancies(openVacancies);
      setApplications(myApplications);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadStudentData(); }, [session.accessToken]);

  const appliedVacancyIds = new Set(applications.map((application) => application.vacancyId));
  const normalizedSearch = search.trim().toLowerCase();
  const filteredVacancies = vacancies.filter((vacancy) => {
    const matchesSearch = !normalizedSearch || [vacancy.title, vacancy.companyName, vacancy.location, ...vacancy.skills]
      .some((value) => value.toLowerCase().includes(normalizedSearch));
    return matchesSearch && (workMode === 'ALL' || vacancy.workMode === workMode);
  });
  const interviewCount = applications.filter((application) => application.status === 'INTERVIEW').length;

  async function submitApplication(event) {
    event.preventDefault();
    if (!selectedVacancy) return;
    setApplying(true);
    setError('');
    setNotice('');
    try {
      await vacancyApi('/api/student/applications', session.accessToken, {
        method: 'POST',
        body: JSON.stringify({ vacancyId: selectedVacancy.id, coverLetter }),
      });
      setSelectedVacancy(null);
      setCoverLetter('');
      setNotice(`Your application for ${selectedVacancy.title} has been submitted.`);
      await loadStudentData();
      setView('applications');
    } catch (applyError) {
      setError(applyError.message);
    } finally {
      setApplying(false);
    }
  }

  return <>
    <div className="dashboard-metrics student-metrics">
      <article className="dashboard-metric"><span className="dashboard-metric-icon">↗</span><span>Applications</span><b>{applications.length}</b></article>
      <article className="dashboard-metric"><span className="dashboard-metric-icon">◷</span><span>Interviews</span><b>{interviewCount}</b></article>
      <article className="dashboard-metric"><span className="dashboard-metric-icon">✳</span><span>Profile status</span><b>New</b></article>
    </div>
    <section className="student-workspace">
      <div className="student-workspace-heading"><div><p className="eyebrow">YOUR NEXT OPPORTUNITY</p><h2>Find an internship</h2><p>Explore open roles and keep track of the ones you apply to.</p></div></div>
      {notice && <p className="form-message success student-notice" role="status">{notice}</p>}
      {error && <p className="form-message error student-notice" role="alert">{error}</p>}
      <div className="student-tabs" role="tablist" aria-label="Student dashboard sections">
        <button type="button" role="tab" aria-selected={view === 'vacancies'} className={view === 'vacancies' ? 'active' : ''} onClick={() => { setView('vacancies'); setNotice(''); }}>Browse vacancies <span>{vacancies.length}</span></button>
        <button type="button" role="tab" aria-selected={view === 'applications'} className={view === 'applications' ? 'active' : ''} onClick={() => { setView('applications'); setNotice(''); }}>My applications <span>{applications.length}</span></button>
      </div>
      {view === 'applications' && <div className="student-applications-toolbar"><span>Your application progress</span><button type="button" onClick={loadStudentData} disabled={loading}>{loading ? 'Refreshing…' : 'Refresh status ↻'}</button></div>}
      {view === 'vacancies' ? <>
        <div className="vacancy-search-row"><label className="vacancy-search"><span>⌕</span><input type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search job title, company, skill, or location" aria-label="Search vacancies" /></label><label className="work-mode-filter"><span>Work arrangement</span><select value={workMode} onChange={(event) => setWorkMode(event.target.value)}><option value="ALL">All types</option><option value="ON_SITE">On-site</option><option value="HYBRID">Hybrid</option><option value="REMOTE">Remote</option></select></label></div>
        {loading ? <div className="student-empty-state">Finding open internships…</div> : filteredVacancies.length === 0 ? <div className="student-empty-state"><div className="empty-art"><span>✳</span><i /><i /><i /></div><h3>{vacancies.length ? 'No vacancies match that search.' : 'New opportunities are on their way.'}</h3><p>{vacancies.length ? 'Try another job title, skill, location, or work arrangement.' : 'Check back soon as companies publish new internships.'}</p></div> : <div className="student-vacancy-grid">{filteredVacancies.map((vacancy) => <article className="student-vacancy-card" key={vacancy.id}>
          <div className="student-vacancy-top"><span className="company-mark lavender">{vacancy.companyName.charAt(0).toUpperCase()}</span><span className="vacancy-status open"><i />Open</span></div>
          <p className="company-name">{vacancy.companyName}</p><h3>{vacancy.title}</h3>
          <div className="vacancy-details"><span>⌖ {vacancy.location}</span><span>◷ {vacancy.workMode.replace('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase())}</span></div>
          <p className="student-vacancy-summary">{vacancy.description}</p>
          <div className="vacancy-skills">{vacancy.skills.slice(0, 4).map((skill) => <span className="category-pill" key={skill}>{skill}</span>)}{vacancy.skills.length > 4 && <span className="category-pill">+{vacancy.skills.length - 4}</span>}</div>
          <div className="student-vacancy-footer"><span>Apply by {new Date(`${vacancy.deadline}T00:00:00`).toLocaleDateString()}</span><button type="button" onClick={() => { setSelectedVacancy(vacancy); setCoverLetter(''); }}>View details <ArrowUpRight /></button></div>
        </article>)}</div>}
      </> : applications.length === 0 ? <div className="student-empty-state"><div className="empty-art"><span>↗</span><i /><i /><i /></div><h3>Your applications will show up here.</h3><p>Find an internship you like and submit your first application.</p><button className="button button-purple" type="button" onClick={() => setView('vacancies')}>Browse vacancies <ArrowUpRight /></button></div> : <div className="student-applications-list">{applications.map((application) => <article className="student-application-card" key={application.id}><span className="company-mark lavender">{application.companyName.charAt(0).toUpperCase()}</span><div className="student-application-info"><h3>{application.vacancyTitle}</h3><p>{application.companyName} · Applied {new Date(application.appliedAt).toLocaleDateString()}</p></div><span className={`application-status ${application.status.toLowerCase()}`}>{application.status.replace('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase())}</span></article>)}</div>}
    </section>
    {selectedVacancy && <div className="vacancy-modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setSelectedVacancy(null); }}><section className="vacancy-modal" role="dialog" aria-modal="true" aria-labelledby="vacancy-modal-title"><button type="button" className="vacancy-modal-close" aria-label="Close vacancy details" onClick={() => setSelectedVacancy(null)}>×</button><span className="vacancy-status open"><i />Open</span><p className="company-name">{selectedVacancy.companyName}</p><h2 id="vacancy-modal-title">{selectedVacancy.title}</h2><div className="vacancy-details"><span>⌖ {selectedVacancy.location}</span><span>◷ {selectedVacancy.workMode.replace('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase())}</span><span>Apply by {new Date(`${selectedVacancy.deadline}T00:00:00`).toLocaleDateString()}</span></div><h3>About this internship</h3><p className="vacancy-modal-description">{selectedVacancy.description}</p><h3>Skills and qualifications</h3><div className="vacancy-skills">{selectedVacancy.skills.map((skill) => <span className="category-pill" key={skill}>{skill}</span>)}</div>{appliedVacancyIds.has(selectedVacancy.id) ? <p className="form-message info">You have already applied for this vacancy. Check My applications for updates.</p> : <form className="student-apply-form" onSubmit={submitApplication}><label>Short note to the company <span>(optional)</span><textarea value={coverLetter} onChange={(event) => setCoverLetter(event.target.value)} rows="4" maxLength="3000" placeholder="Tell the company why this internship interests you." /></label><button className="button button-purple" type="submit" disabled={applying}>{applying ? 'Submitting…' : 'Submit application'} <ArrowUpRight /></button></form>}</section></div>}
  </>;
}

function Dashboard({ session, onLogout, onHome }) {
  const isCompany = session.user.role === 'COMPANY';
  const firstName = session.user.name.split(' ')[0];
  const [vacancies, setVacancies] = useState([]);
  const [companyApplications, setCompanyApplications] = useState([]);
  const [vacanciesLoading, setVacanciesLoading] = useState(isCompany);
  const [vacancyError, setVacancyError] = useState('');
  const [vacancyMessage, setVacancyMessage] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [vacancyDraft, setVacancyDraft] = useState(emptyVacancy);
  const [savingVacancy, setSavingVacancy] = useState(false);
  const [statusSavingId, setStatusSavingId] = useState('');
  const [reviewFilter, setReviewFilter] = useState('ALL');

  async function loadVacancies() {
    setVacanciesLoading(true);
    setVacancyError('');
    try {
      const [companyVacancies, applications] = await Promise.all([
        vacancyApi('/api/company/vacancies', session.accessToken),
        vacancyApi('/api/company/applications', session.accessToken),
      ]);
      setVacancies(companyVacancies);
      setCompanyApplications(applications);
    } catch (error) {
      setVacancyError(error.message);
    } finally {
      setVacanciesLoading(false);
    }
  }

  useEffect(() => {
    if (isCompany) loadVacancies();
  }, [isCompany, session.accessToken]);

  const openVacancies = vacancies.filter((vacancy) => vacancy.status === 'OPEN').length;
  const interviewCount = companyApplications.filter((application) => application.status === 'INTERVIEW').length;
  const metrics = [{ label: 'Open vacancies', value: String(openVacancies), icon: '▤' }, { label: 'Applications', value: String(companyApplications.length), icon: '↗' }, { label: 'Interviews', value: String(interviewCount), icon: '◷' }];
  const filteredApplications = reviewFilter === 'ALL' ? companyApplications : companyApplications.filter((application) => application.vacancyId === reviewFilter);

  function startEditing(vacancy) {
    setEditingId(vacancy.id);
    setVacancyDraft({
      title: vacancy.title,
      description: vacancy.description,
      skills: vacancy.skills.join(', '),
      location: vacancy.location,
      workMode: vacancy.workMode,
      deadline: vacancy.deadline,
    });
    setVacancyMessage('');
  }

  function cancelEditing() {
    setEditingId(null);
    setVacancyDraft(emptyVacancy());
  }

  async function saveVacancy(event) {
    event.preventDefault();
    setSavingVacancy(true);
    setVacancyError('');
    setVacancyMessage('');
    const payload = {
      ...vacancyDraft,
      skills: vacancyDraft.skills.split(',').map((skill) => skill.trim()).filter(Boolean),
    };
    try {
      await vacancyApi(editingId ? `/api/company/vacancies/${editingId}` : '/api/company/vacancies', session.accessToken, {
        method: editingId ? 'PUT' : 'POST',
        body: JSON.stringify(payload),
      });
      setVacancyMessage(editingId ? 'Vacancy updated.' : 'Vacancy published. Students can now find it.');
      cancelEditing();
      await loadVacancies();
    } catch (error) {
      setVacancyError(error.message);
    } finally {
      setSavingVacancy(false);
    }
  }

  async function toggleVacancyStatus(vacancy) {
    setVacancyError('');
    setVacancyMessage('');
    try {
      await vacancyApi(`/api/company/vacancies/${vacancy.id}/status`, session.accessToken, {
        method: 'PATCH',
        body: JSON.stringify({ status: vacancy.status === 'OPEN' ? 'CLOSED' : 'OPEN' }),
      });
      setVacancyMessage(vacancy.status === 'OPEN' ? 'Vacancy closed.' : 'Vacancy reopened.');
      await loadVacancies();
    } catch (error) {
      setVacancyError(error.message);
    }
  }

  async function updateApplicationStatus(application, status) {
    setStatusSavingId(application.id);
    setVacancyError('');
    try {
      await vacancyApi(`/api/company/applications/${application.id}/status`, session.accessToken, {
        method: 'PATCH',
        body: JSON.stringify({ status }),
      });
      await loadVacancies();
    } catch (error) {
      setVacancyError(error.message);
    } finally {
      setStatusSavingId('');
    }
  }

  return (
    <main className="dashboard-shell">
      <header className="dashboard-topbar"><a className="brand" href="#top" onClick={(event) => { event.preventDefault(); onHome(); }}><span className="brand-symbol"><span /></span><span>Career<span className="brand-is">IS</span></span></a><div className="dashboard-account"><span className="dashboard-avatar">{firstName.charAt(0).toUpperCase()}</span><span><b>{session.user.name}</b><small>{isCompany ? 'Company account' : 'Student account'}</small></span><button type="button" onClick={onLogout}>Sign out</button></div></header>
      <section className="dashboard-content">
        <p className="eyebrow">{isCompany ? 'COMPANY WORKSPACE' : 'STUDENT WORKSPACE'}</p>
        <div className="dashboard-heading"><div><h1>Welcome, {firstName}.</h1><p>{isCompany ? 'Your company space is ready to meet its next intern.' : 'Your next chapter starts with a few small steps.'}</p></div><span className="dashboard-date">CAREERIS · {isCompany ? 'COMPANY' : 'STUDENT'}</span></div>
        {isCompany && <div className="dashboard-metrics">{metrics.map((metric) => <article className="dashboard-metric" key={metric.label}><span className="dashboard-metric-icon">{metric.icon}</span><span>{metric.label}</span><b>{metric.value}</b></article>)}</div>}
        {isCompany ? (
          <section className="vacancy-workspace">
            <div className="vacancy-section-heading"><div><p className="eyebrow">YOUR OPPORTUNITIES</p><h2>Vacancies</h2><p>Create a listing and invite students to apply.</p></div><button className="button button-purple vacancy-new-button" type="button" onClick={() => { cancelEditing(); setVacancyMessage(''); }}>＋ Post a vacancy</button></div>
            {vacancyMessage && <p className="form-message success" role="status">{vacancyMessage}</p>}
            {vacancyError && <p className="form-message error" role="alert">{vacancyError}</p>}
            {editingId !== null && <p className="vacancy-editing-note">Editing a vacancy <button type="button" onClick={cancelEditing}>Cancel edit</button></p>}
            <form className="vacancy-form" onSubmit={saveVacancy}>
              <div className="vacancy-form-heading"><div><span className="vacancy-form-icon">✳</span><div><h3>{editingId ? 'Update vacancy' : 'Create a vacancy'}</h3><p>Share the details students need before applying.</p></div></div>{editingId && <button type="button" className="vacancy-cancel" onClick={cancelEditing}>Cancel</button>}</div>
              <div className="vacancy-fields">
                <label>Internship title<input value={vacancyDraft.title} onChange={(event) => setVacancyDraft({ ...vacancyDraft, title: event.target.value })} placeholder="e.g. Software Engineering Intern" maxLength="120" required /></label>
                <label>Location<input value={vacancyDraft.location} onChange={(event) => setVacancyDraft({ ...vacancyDraft, location: event.target.value })} placeholder="e.g. Colombo or Remote" maxLength="120" required /></label>
                <label>Work arrangement<select value={vacancyDraft.workMode} onChange={(event) => setVacancyDraft({ ...vacancyDraft, workMode: event.target.value })}><option value="ON_SITE">On-site</option><option value="HYBRID">Hybrid</option><option value="REMOTE">Remote</option></select></label>
                <label>Application deadline<input type="date" min={new Date().toISOString().slice(0, 10)} value={vacancyDraft.deadline} onChange={(event) => setVacancyDraft({ ...vacancyDraft, deadline: event.target.value })} required /></label>
                <label className="vacancy-wide">Skills <span>(comma separated)</span><input value={vacancyDraft.skills} onChange={(event) => setVacancyDraft({ ...vacancyDraft, skills: event.target.value })} placeholder="e.g. Java, communication, teamwork" required /></label>
                <label className="vacancy-wide">Description<textarea value={vacancyDraft.description} onChange={(event) => setVacancyDraft({ ...vacancyDraft, description: event.target.value })} placeholder="Describe the work, what the intern will learn, and any requirements." rows="5" maxLength="5000" required /></label>
              </div>
              <div className="vacancy-form-footer"><span>Deadline must be today or later.</span><button className="button button-purple" type="submit" disabled={savingVacancy}>{savingVacancy ? 'Saving…' : editingId ? 'Save changes' : 'Publish vacancy'} <ArrowUpRight /></button></div>
            </form>
            <div className="vacancy-list-heading"><h3>Posted by your company</h3><span>{vacancies.length} {vacancies.length === 1 ? 'vacancy' : 'vacancies'}</span></div>
            {vacanciesLoading ? <div className="vacancy-list-placeholder">Loading your vacancies…</div> : vacancies.length === 0 ? (
              <div className="vacancy-list-placeholder"><div className="empty-art"><span>✳</span><i /><i /><i /></div><h3>Your first listing starts here.</h3><p>Post an internship and it will appear here and in the student vacancy feed.</p></div>
            ) : <div className="vacancy-list">{vacancies.map((vacancy) => <article className={`vacancy-card ${vacancy.status === 'CLOSED' ? 'closed' : ''}`} key={vacancy.id}>
              <div className="vacancy-card-top"><span className="company-mark lavender">{session.user.name.charAt(0).toUpperCase()}</span><span className={`vacancy-status ${vacancy.status.toLowerCase()}`}><i />{vacancy.status === 'OPEN' ? 'Open' : 'Closed'}</span></div>
              <h3>{vacancy.title}</h3><p className="vacancy-company-name">{vacancy.companyName}</p>
              <div className="vacancy-details"><span>⌖ {vacancy.location}</span><span>◷ {vacancy.workMode.replace('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase())}</span><span>Apply by {new Date(`${vacancy.deadline}T00:00:00`).toLocaleDateString()}</span></div>
              <p className="vacancy-description">{vacancy.description}</p>
              <div className="vacancy-skills">{vacancy.skills.map((skill) => <span className="category-pill" key={skill}>{skill}</span>)}</div>
              <div className="vacancy-card-actions"><button type="button" className="vacancy-edit" onClick={() => startEditing(vacancy)}>Edit listing</button><button type="button" className={vacancy.status === 'OPEN' ? 'vacancy-close' : 'vacancy-reopen'} onClick={() => toggleVacancyStatus(vacancy)}>{vacancy.status === 'OPEN' ? 'Close vacancy' : 'Reopen vacancy'}</button></div>
            </article>)}</div>}
            <section className="company-review-section">
              <div className="vacancy-list-heading"><div><h3>Applications</h3><p>Review applicants and update their progress.</p></div><label className="review-filter"><span>Vacancy</span><select value={reviewFilter} onChange={(event) => setReviewFilter(event.target.value)}><option value="ALL">All vacancies</option>{vacancies.map((vacancy) => <option key={vacancy.id} value={vacancy.id}>{vacancy.title}</option>)}</select></label></div>
              {vacanciesLoading ? <div className="vacancy-list-placeholder">Loading applications…</div> : filteredApplications.length === 0 ? <div className="vacancy-list-placeholder"><div className="empty-art"><span>↗</span><i /><i /><i /></div><h3>No applications yet.</h3><p>When students apply, their details will appear here for review.</p></div> : <div className="company-application-list">{filteredApplications.map((application) => <article className="company-application-card" key={application.id}>
                <div className="company-application-main"><span className="company-mark lavender">{application.studentName.charAt(0).toUpperCase()}</span><div className="company-application-identity"><h4>{application.studentName}</h4><a href={`mailto:${application.studentEmail}`}>{application.studentEmail}</a><p>{application.vacancyTitle} · Applied {new Date(application.appliedAt).toLocaleDateString()}</p></div><span className={`application-status ${application.status.toLowerCase()}`}>{application.status.replace('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase())}</span></div>
                <div className="company-application-note"><b>Note to the company</b><p>{application.coverLetter || 'The student did not include a note.'}</p></div>
                <div className="company-review-actions"><span>Update status</span><div><button type="button" className={application.status === 'UNDER_REVIEW' ? 'selected' : ''} disabled={statusSavingId === application.id || application.status === 'UNDER_REVIEW'} onClick={() => updateApplicationStatus(application, 'UNDER_REVIEW')}>{statusSavingId === application.id ? 'Saving…' : 'Under review'}</button><button type="button" className={application.status === 'INTERVIEW' ? 'selected interview' : 'interview'} disabled={statusSavingId === application.id || application.status === 'INTERVIEW'} onClick={() => updateApplicationStatus(application, 'INTERVIEW')}>{statusSavingId === application.id ? 'Saving…' : 'Invite to interview'}</button><button type="button" className={application.status === 'REJECTED' ? 'selected rejected' : 'rejected'} disabled={statusSavingId === application.id || application.status === 'REJECTED'} onClick={() => updateApplicationStatus(application, 'REJECTED')}>{statusSavingId === application.id ? 'Saving…' : 'Reject'}</button></div></div>
              </article>)}</div>}
            </section>
          </section>
        ) : <StudentWorkspace session={session} />}
      </section>
      <footer className="dashboard-footer"><span>© 2026 CareerIS</span><button type="button" onClick={onHome}>Back to home</button></footer>
    </main>
  );
}

function loadSession() {
  try {
    const saved = JSON.parse(window.sessionStorage.getItem('careeris.session'));
    if (saved?.expiresAt && Date.parse(saved.expiresAt) <= Date.now()) {
      window.sessionStorage.removeItem('careeris.session');
      return null;
    }
    return saved;
  } catch {
    return null;
  }
}

function App() {
  const [auth, setAuth] = useState(authRouteFromHash);
  const [session, setSession] = useState(loadSession);

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

  function saveSession(loginResponse) {
    window.sessionStorage.setItem('careeris.session', JSON.stringify(loginResponse));
    setSession(loginResponse);
    openAuth('dashboard', loginResponse.user.role.toLowerCase());
  }

  function goHome() {
    window.history.pushState(null, '', window.location.pathname);
    setAuth(null);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function signOut() {
    window.sessionStorage.removeItem('careeris.session');
    const role = session?.user?.role?.toLowerCase() || 'student';
    setSession(null);
    openAuth('sign-in', role);
  }

  if (auth) {
    if (auth.mode === 'dashboard') {
      if (!session || session.user.role.toLowerCase() !== auth.role) {
        return <AuthScreen mode="sign-in" role={auth.role} onModeChange={(mode) => openAuth(mode, auth.role)} onRoleChange={(role) => openAuth('sign-in', role)} onHome={goHome} onLogin={saveSession} />;
      }
      return <Dashboard session={session} onLogout={signOut} onHome={goHome} />;
    }
    return <AuthScreen mode={auth.mode} role={auth.role} onModeChange={(mode) => openAuth(mode, auth.role)} onRoleChange={(role) => openAuth(auth.mode, role)} onHome={goHome} onLogin={saveSession} />;
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
