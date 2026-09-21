const form = document.querySelector('#decision-form');
const result = document.querySelector('#result');
const button = document.querySelector('#decide-button');
const error = document.querySelector('#form-error');

fetch('/api/status').then(response => response.json()).then(status => {
  document.querySelector('#mode-status').textContent = `${status.mode} MODE`;
});

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  error.textContent = '';
  button.disabled = true;
  button.querySelector('span').textContent = 'DECIDING…';
  result.classList.add('is-loading');
  result.scrollIntoView({ behavior: 'smooth' });

  const data = new FormData(form);
  try {
    const response = await fetch('/api/decision', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ location: data.get('location'), time: data.get('time'), mood: data.get('mood') })
    });
    const payload = await response.json();
    if (!response.ok) throw new Error(payload.error || 'The skies are not answering. Try again.');
    render(payload);
  } catch (cause) {
    error.textContent = cause.message;
    form.scrollIntoView({ behavior: 'smooth', block: 'center' });
  } finally {
    result.classList.remove('is-loading');
    button.disabled = false;
    button.querySelector('span').textContent = 'DECIDE';
  }
});

function render(data) {
  document.querySelector('#mode-status').textContent = `${data.mode} MODE`;
  document.querySelector('#weather-location').textContent = `${data.weather.location} · ${data.weather.localTime}`;
  document.querySelector('#weather-temperature').textContent = `${Math.round(data.weather.temperature)}°`;
  document.querySelector('#weather-rain').textContent = `${data.weather.rainChance}%`;
  document.querySelector('#weather-wind').textContent = Math.round(data.weather.windSpeed);
  document.querySelector('#verdict-meta').textContent = `${data.mode} · ${data.probability}% YES`;
  document.querySelector('#verdict-icon').textContent = weatherIcon(data.weather.weatherCode, data.weather.daylight);
  document.querySelector('#headline').textContent = data.headline;
  document.querySelector('#accent').textContent = data.accent;
  document.querySelector('#verdict-message').textContent = data.message;
  document.querySelector('#activity-label').textContent = data.activityLabel;
  document.querySelector('#duration').textContent = data.duration;
  document.querySelector('#decision-note').textContent = data.note;
}

function weatherIcon(code, daylight) {
  if (code >= 80) return 'ϟ';
  if (code >= 51) return '☂';
  if (code > 3) return '≋';
  return daylight ? '☀' : '☾';
}
