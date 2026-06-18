export interface ApiErrorResponse {
  status?: number;
  error?: string;
  message?: string;
  fields?: Array<{ field?: string; message?: string }>;
}

export function getApiErrorMessage(error: unknown, fallbackMessage: string): string {
  const response = extractApiError(error);

  if (response?.fields?.length) {
    return response.fields
      .map(field => field.message)
      .filter((message): message is string => !!message)
      .join(' ');
  }

  return response?.message?.trim() || fallbackMessage;
}

function extractApiError(error: unknown): ApiErrorResponse | null {
  if (!error || typeof error !== 'object') {
    return null;
  }

  const candidate = error as { error?: unknown };
  if (!candidate.error || typeof candidate.error !== 'object') {
    return null;
  }

  return candidate.error as ApiErrorResponse;
}

