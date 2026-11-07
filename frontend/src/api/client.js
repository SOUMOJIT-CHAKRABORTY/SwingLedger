let csrf;
export async function api(path, method = "GET", body) {
  if (method !== "GET" && !csrf) {
    const r = await fetch("/api/v1/auth/csrf");
    csrf = (await r.json()).token;
  }
  const r = await fetch("/api/v1" + path, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(method !== "GET" ? { "X-CSRF-TOKEN": csrf } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await r.text();
  let data;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = null;
  }
  if (!r.ok) {
    if (r.status === 401) window.dispatchEvent(new Event("session-expired"));
    throw new Error(
      data?.message ||
        (r.status === 403
          ? "Your session expired. Refresh and try again."
          : "Request failed. Please try again."),
    );
  }
  return data;
}

export function resetCsrfToken() {
  csrf = null;
}
