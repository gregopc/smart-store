export interface PaginatedApiResponse<T> extends ApiResponseMetaData {
  content: T[]
}

export interface ApiResponseMetaData {
  pageable: {
      pageNumber: number,
      pageSize: number,
      sort: {
        empty: boolean,
        sorted: boolean,
        unsorted: boolean
      },
      offset: number,
      paged: boolean,
      unpaged: boolean
    },
    last: boolean,
    totalPages: number,
    totalElements: number,
    first: boolean,
    size: number,
    number: number,
    sort: {
      empty: boolean,
      sorted: boolean,
      unsorted: boolean
    },
    numberOfElements: number,
    empty: boolean
}
