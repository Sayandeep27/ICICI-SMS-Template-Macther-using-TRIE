const messageInput = document.getElementById("messageInput");
const sampleSelect = document.getElementById("sampleSelect");
const loadSampleBtn = document.getElementById("loadSampleBtn");
const matchBtn = document.getElementById("matchBtn");
const resetBtn = document.getElementById("resetBtn");
const refreshTemplatesBtn = document.getElementById("refreshTemplatesBtn");
const refreshResultsBtn = document.getElementById("refreshResultsBtn");
const templatesList = document.getElementById("templatesList");
const resultsList = document.getElementById("resultsList");
const statusBadge = document.getElementById("statusBadge");
const matchedTemplateId = document.getElementById("matchedTemplateId");
const matchedValues = document.getElementById("matchedValues");
const matchedMessage = document.getElementById("matchedMessage");
const templateCount = document.getElementById("templateCount");
const resultCount = document.getElementById("resultCount");

const samples = {
  "2": "Amount Rs 500 debite to your account",
  "8": "Dear Rahul, Ankit has applied for an ICICI Bank Plus Card for you. To complete the process and get your card, please click on the consent link sent to your email.",
  "24": "By entering OTP, you consent for loan booking, EMI debit from ICICI Bank <SBI123> Acc, Key Fact Statement & accept Cardless EMI T&Cs. Visit https://example.com/loan."
};

function setStatus(kind, text) {
  statusBadge.className = `badge ${kind}`;
  statusBadge.textContent = text;
}

function setMatchedResult(data) {
  matchedTemplateId.textContent = data?.templateId ?? "-";
  matchedValues.textContent = data?.extractedValues || "-";
  matchedMessage.textContent = data?.incomingMessage || "Paste a message and match it to see the result here.";
}

function previewTemplate(text, maxLen = 110) {
  return text.length > maxLen ? `${text.slice(0, maxLen).trim()}...` : text;
}

async function apiJson(path, options) {
  const response = await fetch(path, {
    headers: { "Content-Type": "application/json" },
    ...options
  });

  const contentType = response.headers.get("content-type") || "";
  const body = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message = typeof body === "string" ? body : body?.message || "Request failed";
    throw new Error(message);
  }

  return body;
}

function renderList(container, items, renderItem, emptyText) {
  if (!items || items.length === 0) {
    container.innerHTML = `<div class="empty-state">${emptyText}</div>`;
    return;
  }

  container.innerHTML = items.map(renderItem).join("");
}

async function loadTemplates() {
  try {
    const templates = await apiJson("/api/templates");
    templateCount.textContent = templates.length;

    renderList(
      templatesList,
      templates,
      (item) => `
        <div class="list-item">
          <div class="list-item-top">
            <span class="pill">ID ${item.id}</span>
          </div>
          <p class="preview">${previewTemplate(item.templateText)}</p>
        </div>
      `,
      "No templates found."
    );
  } catch (error) {
    templatesList.innerHTML = `<div class="error-state">Could not load templates: ${error.message}</div>`;
  }
}

async function loadResults() {
  try {
    const results = await apiJson("/api/results");
    resultCount.textContent = results.length;

    const latest = results.slice().reverse().slice(0, 6);
    renderList(
      resultsList,
      latest,
      (item) => `
        <div class="list-item">
          <div class="list-item-top">
            <span class="pill">Template ${item.templateId}</span>
            <span class="pill">#${item.id}</span>
          </div>
          <p class="preview">${previewTemplate(item.incomingMessage, 80)}</p>
          <p class="preview"><strong>Values:</strong> ${item.extractedValues || "-"}</p>
        </div>
      `,
      "No match results yet."
    );
  } catch (error) {
    resultsList.innerHTML = `<div class="error-state">Could not load results: ${error.message}</div>`;
  }
}

function loadSample() {
  const selected = sampleSelect.value;
  messageInput.value = samples[selected] || "";
}

async function matchMessage() {
  const message = messageInput.value.trim();

  if (!message) {
    setStatus("error", "Empty");
    setMatchedResult(null);
    matchedMessage.textContent = "Please paste an SMS message first.";
    return;
  }

  matchBtn.disabled = true;
  setStatus("idle", "Matching...");

  try {
    const result = await apiJson("/api/match", {
      method: "POST",
      body: JSON.stringify({ message })
    });

    setMatchedResult(result);
    setStatus("success", "Matched");
    await loadResults();
  } catch (error) {
    setStatus("error", "Failed");
    matchedTemplateId.textContent = "-";
    matchedValues.textContent = "-";
    matchedMessage.textContent = error.message;
  } finally {
    matchBtn.disabled = false;
  }
}

function resetDemo() {
  messageInput.value = "";
  setStatus("idle", "Waiting");
  setMatchedResult(null);
}

loadSampleBtn.addEventListener("click", loadSample);
matchBtn.addEventListener("click", matchMessage);
resetBtn.addEventListener("click", resetDemo);
refreshTemplatesBtn.addEventListener("click", loadTemplates);
refreshResultsBtn.addEventListener("click", loadResults);

messageInput.value = samples[sampleSelect.value];
loadTemplates();
loadResults();
