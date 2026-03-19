export const toPageParams = (page = 1, size = 10) => ({
  page: Math.max(1, page),
  size: Math.max(1, size),
});

export const normalizePageResponse = (response) => {
  const items = response?.items || response?.content || [];
  const totalItems = response?.totalItems ?? response?.totalElements ?? items.length;
  const page = response?.page ?? 1;
  const size = response?.size ?? items.length;
  const totalPages = response?.totalPages ?? Math.max(1, Math.ceil(totalItems / Math.max(1, size)));

  return {
    items,
    totalItems,
    page,
    size,
    totalPages,
    hasNext: page < totalPages,
    hasPrev: page > 1,
  };
};
