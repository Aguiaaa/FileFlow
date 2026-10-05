const filesContainer = document.getElementById("files");
const fileInput = document.getElementById("fileInput");
const uploadButton = document.getElementById("uploadButton");
const shareLinksContainer = document.getElementById("shareLinks");
const requestLog = document.getElementById("requestLog");
const searchInput = document.getElementById("searchInput");
const sortSelect = document.getElementById("sortSelect");
const selectedFile = document.getElementById("selectedFile");
const refreshButton = document.getElementById("refreshButton");
const lastRequest = document.getElementById("lastRequest");

const statistics = {
    files: document.getElementById("filesCount"),
    totalSize: document.getElementById("totalSize"),
    links: document.getElementById("linksCount")
};

let files = [];

async function apiRequest(
    url,
    options = {},
    description = "Request sent by the interface",
    showAsLastRequest = true
) {
    const method = options.method || "GET";
    const startedAt = performance.now();
    let response;

    try {
        response = await fetch(url, options);
    } catch (error) {
        addRequestLog(
            method,
            url,
            "NETWORK ERROR",
            performance.now() - startedAt,
            description,
            showAsLastRequest
        );
        throw error;
    }

    addRequestLog(
        method,
        url,
        `${response.status} ${response.statusText}`,
        performance.now() - startedAt,
        description,
        showAsLastRequest
    );

    return response;
}

function addRequestLog(
    method,
    url,
    status,
    duration,
    description = "Browser navigation",
    showAsLastRequest = true
) {
    const emptyState = requestLog.querySelector(".empty-state");

    if (emptyState) {
        emptyState.remove();
    }

    const entry = document.createElement("div");
    entry.className = "request-entry";
    entry.innerHTML = `
        <span class="request-method method-${method.toLowerCase()}">${method}</span>
        <code>${url}</code>
        <span class="request-status">${status}</span>
        <span class="request-duration">${Math.round(duration)} ms</span>
    `;

    requestLog.prepend(entry);
    if (!showAsLastRequest) {
        return;
    }

    lastRequest.innerHTML = `
        <strong>${description}</strong>
        <div class="last-request-details">
            <span class="request-method method-${method.toLowerCase()}">${method}</span>
            <code>${url}</code>
            <span class="request-status">${status}</span>
            <span class="request-duration">${Math.round(duration)} ms</span>
        </div>
        <p>${explainRequest(method, url, status)}</p>
    `;
}

function explainRequest(method, url, status) {
    if (status === "NETWORK ERROR") {
        return "The browser could not reach the server. Check that the application is running.";
    }

    if (method === "POST" && url === "/files/upload") {
        return "The selected file was sent to the server so it could be stored.";
    }

    if (method === "POST" && url.includes("/share")) {
        return "The server created a temporary sharing link for this file.";
    }

    if (method === "GET" && url === "/files") {
        return "The interface asked the server for the current list of files.";
    }

    if (method === "DELETE") {
        return "The server deleted the file and its associated data.";
    }

    if (method === "GET" && url.includes("/download")) {
        return "The browser requested the file contents for download.";
    }

    return status.startsWith("2")
        ? "The request completed successfully."
        : "The server returned an error. Read the status code to investigate.";
}

async function loadFiles(showAsLastRequest = true) {
    setLoading(filesContainer, "Loading files...");

    try {
        const response = await apiRequest(
            "/files",
            {},
            "Loading the file list",
            showAsLastRequest
        );

        if (!response.ok) {
            throw new Error(`Unable to load files (${response.status})`);
        }

        files = await response.json();
        renderFiles();
        updateStatistics();
    } catch (error) {
        showError(filesContainer, error.message);
    }
}

function renderFiles() {
    const query = searchInput.value.trim().toLowerCase();
    const visibleFiles = files
        .filter(file => file.name.toLowerCase().includes(query))
        .sort((first, second) => compareFiles(first, second, sortSelect.value));

    filesContainer.innerHTML = "";

    if (visibleFiles.length === 0) {
        filesContainer.innerHTML = `<p class="empty-state">${
            query ? "No file matches your search." : "No files uploaded yet."
        }</p>`;
        return;
    }

    for (const file of visibleFiles) {
        const element = document.createElement("article");
        element.className = "file";
        element.innerHTML = `
            <div class="file-info">
                <strong></strong>
                <span class="file-size">${formatSize(file.size)}</span>
                <span class="file-type">${file.contentType || "Unknown type"}</span>
            </div>
            <div class="actions">
                <button class="secondary-button" data-action="download">Download</button>
                <button class="secondary-button" data-action="share">Share</button>
                <button class="delete-button" data-action="delete">Delete</button>
            </div>
        `;

        element.querySelector("strong").textContent = file.name;
        element.querySelector('[data-action="download"]')
            .addEventListener("click", () => downloadFile(file.id));
        element.querySelector('[data-action="share"]')
            .addEventListener("click", () => shareFile(file.id));
        element.querySelector('[data-action="delete"]')
            .addEventListener("click", () => deleteFile(file.id));
        filesContainer.appendChild(element);
    }
}

function compareFiles(first, second, sort) {
    if (sort === "size") {
        return second.size - first.size;
    }

    if (sort === "name") {
        return first.name.localeCompare(second.name);
    }

    return 0;
}

async function uploadFile() {
    const file = fileInput.files[0];

    if (!file) {
        showNotice("Choose a file before uploading.", "warning");
        return;
    }

    uploadButton.disabled = true;
    uploadButton.textContent = "Uploading...";

    const formData = new FormData();
    formData.append("file", file);

    try {
        const response = await apiRequest("/files/upload", {
            method: "POST",
            body: formData
        }, "Uploading a file");

        if (!response.ok) {
            throw new Error(`Upload failed (${response.status})`);
        }

        fileInput.value = "";
        selectedFile.textContent = "No file selected";
        showNotice("File uploaded successfully.", "success");
        await loadFiles(false);
    } catch (error) {
        showNotice(error.message, "error");
    } finally {
        uploadButton.disabled = false;
        uploadButton.textContent = "Upload";
    }
}

async function deleteFile(id) {
    if (!confirm("Delete this file and its shared links?")) {
        return;
    }

    const response = await apiRequest(
        `/files/${id}`,
        { method: "DELETE" },
        "Deleting a file"
    );

    if (!response.ok) {
        showNotice("Could not delete this file.", "error");
        return;
    }

    showNotice("File deleted.", "success");
    await loadFiles(false);
}

function downloadFile(id) {
    addRequestLog(
        "GET",
        `/files/${id}/download`,
        "Navigation started",
        0,
        "Downloading a file"
    );
    window.location.href = `/files/${id}/download`;
}

async function shareFile(id) {
    try {
        const response = await apiRequest(`/files/${id}/share`, {
            method: "POST"
        }, "Creating a share link");

        if (!response.ok) {
            throw new Error(`Could not create share link (${response.status})`);
        }

        const shareLink = await response.json();
        displayShareLink(shareLink);
        await copyToClipboard(shareLink.url);
        showNotice("Share link created and copied.", "success");
    } catch (error) {
        showNotice(error.message, "error");
    }
}

function displayShareLink(shareLink) {
    const emptyState = shareLinksContainer.querySelector(".empty-state");

    if (emptyState) {
        emptyState.remove();
    }

    const element = document.createElement("article");
    element.className = "share-link";
    element.innerHTML = `
        <div>
            <a target="_blank" rel="noopener"></a>
            <span class="share-link-expiration"></span>
        </div>
        <button class="secondary-button">Copy</button>
    `;

    const link = element.querySelector("a");
    link.href = shareLink.url;
    link.textContent = shareLink.url;
    element.querySelector(".share-link-expiration").textContent =
        `Expires: ${formatDate(shareLink.expiresAt)}`;
    element.querySelector("button").addEventListener("click", () => {
        copyToClipboard(shareLink.url);
    });

    shareLinksContainer.prepend(element);
    statistics.links.textContent =
        String(Number(statistics.links.textContent) + 1);
}

async function copyToClipboard(value) {
    await navigator.clipboard.writeText(value);
}

function updateStatistics() {
    statistics.files.textContent = String(files.length);
    statistics.totalSize.textContent =
        formatSize(files.reduce((total, file) => total + file.size, 0));
}

function setLoading(container, message) {
    container.innerHTML = `<p class="empty-state loading">${message}</p>`;
}

function showError(container, message) {
    container.innerHTML = `<p class="empty-state error-text">${message}</p>`;
}

function showNotice(message, type) {
    const notice = document.getElementById("notice");
    notice.textContent = message;
    notice.className = `notice ${type}`;

    window.clearTimeout(showNotice.timeout);
    showNotice.timeout = window.setTimeout(() => {
        notice.className = "notice hidden";
    }, 4000);
}

function formatDate(value) {
    return new Date(value).toLocaleString();
}

function formatSize(bytes) {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

fileInput.addEventListener("change", () => {
    selectedFile.textContent = fileInput.files[0]
        ? `${fileInput.files[0].name} (${formatSize(fileInput.files[0].size)})`
        : "No file selected";
});

uploadButton.addEventListener("click", uploadFile);
searchInput.addEventListener("input", renderFiles);
sortSelect.addEventListener("change", renderFiles);
refreshButton.addEventListener("click", loadFiles);

loadFiles();
