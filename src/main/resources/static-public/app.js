const fileInput = document.getElementById("fileInput");
const selectedFile = document.getElementById("selectedFile");
const shareButton = document.getElementById("shareButton");
const status = document.getElementById("status");
const result = document.getElementById("result");
const shareUrl = document.getElementById("shareUrl");
const expiration = document.getElementById("expiration");
const copyButton = document.getElementById("copyButton");

fileInput.addEventListener("change", () => {
    const file = fileInput.files[0];

    selectedFile.textContent = file
        ? `${file.name} · ${formatSize(file.size)}`
        : "Aucun fichier sélectionné";

    shareButton.disabled = !file;
    result.classList.add("hidden");
    setStatus("");
});

shareButton.addEventListener("click", createShareLink);

copyButton.addEventListener("click", async () => {
    try {
        await navigator.clipboard.writeText(shareUrl.href);
        copyButton.textContent = "Copié !";
        setStatus("Le lien a été copié dans le presse-papiers.", "success");

        window.setTimeout(() => {
            copyButton.textContent = "Copier";
        }, 2000);
    } catch (error) {
        setStatus("La copie automatique a échoué. Copiez le lien manuellement.", "error");
    }
});

async function createShareLink() {
    const file = fileInput.files[0];

    if (!file) {
        return;
    }

    shareButton.disabled = true;
    shareButton.textContent = "Création en cours...";
    result.classList.add("hidden");
    setStatus("Envoi du fichier...", "loading");

    const formData = new FormData();
    formData.append("file", file);

    try {
        const uploadResponse = await fetch("/files/upload", {
            method: "POST",
            body: formData
        });

        if (!uploadResponse.ok) {
            throw new Error("L'envoi du fichier a échoué.");
        }

        const uploadedFile = await uploadResponse.json();
        setStatus("Fichier envoyé. Création du lien...", "loading");

        const shareResponse = await fetch(`/files/${uploadedFile.id}/share`, {
            method: "POST"
        });

        if (!shareResponse.ok) {
            throw new Error("Le lien n'a pas pu être créé.");
        }

        const link = await shareResponse.json();
        showShareLink(link);
        setStatus("Votre lien est prêt.", "success");
    } catch (error) {
        setStatus(error.message, "error");
    } finally {
        shareButton.disabled = false;
        shareButton.textContent = "Créer le lien";
    }
}

function showShareLink(link) {
    shareUrl.href = link.url;
    shareUrl.textContent = link.url;
    expiration.textContent =
        `Expire le ${new Date(link.expiresAt).toLocaleString()}`;
    result.classList.remove("hidden");
}

function setStatus(message, type = "") {
    status.textContent = message;
    status.className = `status ${type}`;
}

function formatSize(bytes) {
    if (bytes < 1024) return `${bytes} o`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} Ko`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} Mo`;
}
