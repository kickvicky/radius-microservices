# Radius — Feed Service API Contract
> Version: v1.0.0 | Base URL: `http://localhost:8080`

---

## 📌 Endpoints Overview

| Method | Path         | Description        |
|--------|--------------|--------------------|
| POST   | `/api/post`  | Create a new post  |
| GET    | `/api/posts` | Fetch all posts    |

---

## 1. Create Post

**`POST /api/post`**

Creates a new geo-tagged post in the feed.

### Request

**Headers**
```
Content-Type: application/json
```

**Body**
```json
{
  "userId":       "a1b2c3d4-0001-4000-8000-aabbccddeeff",
  "userName":     "Filter-Kaapi-9",
  "content":      "Finally a decent Darshini open near Indiranagar 100ft road!",
  "imageUrl":     "https://res.cloudinary.com/example/image.png",
  "tag":          "LOCAL",
  "latitude":     12.9784,
  "longitude":    77.6408,
  "upvoteCount":  0,
  "downvoteCount": 0,
  "commentCount": 0,
  "isVerified":   false
}
```

### Request Field Reference

| Field          | Type      | Required | Constraints          | Description                          |
|----------------|-----------|----------|----------------------|--------------------------------------|
| `userId`       | `UUID`    | ✅ Yes   | Valid UUID           | ID of the user creating the post     |
| `userName`     | `String`  | ✅ Yes   | max 100 chars        | Display name of the user             |
| `content`      | `String`  | ✅ Yes   | Non-empty TEXT       | Body of the post                     |
| `imageUrl`     | `String`  | ❌ No    | max 500 chars        | Optional image URL                   |
| `tag`          | `String`  | ❌ No    | max 50 chars         | Category label (e.g. LOCAL, ALERT)   |
| `latitude`     | `Double`  | ✅ Yes   | Valid coordinate     | Geographic latitude of the post      |
| `longitude`    | `Double`  | ✅ Yes   | Valid coordinate     | Geographic longitude of the post     |
| `upvoteCount`  | `Integer` | ❌ No    | Default: `0`         | Initial upvote count                 |
| `downvoteCount`| `Integer` | ❌ No    | Default: `0`         | Initial downvote count               |
| `commentCount` | `Integer` | ❌ No    | Default: `0`         | Initial comment count                |
| `isVerified`   | `Boolean` | ❌ No    | Default: `false`     | Whether the post is verified/trusted |

> **Note:** `id` and `createdAt` are server-generated — do **not** send them in the request body.

### Response — `201 Created`

```json
{
  "statusCode": "201",
  "statusMsg":  "Post created successfully"
}
```

---

## 2. Fetch All Posts

**`GET /api/posts`**

Returns all posts in the feed as a JSON array.

### Request

No request body or query parameters required.

### Response — `200 OK`

```json
[
  {
    "id":           "e3f1a2b4-77c2-4e1a-bc91-123456789abc",
    "userId":       "a1b2c3d4-0001-4000-8000-aabbccddeeff",
    "userName":     "Filter-Kaapi-9",
    "content":      "Finally a decent Darshini open near Indiranagar 100ft road! The sambar is actually spicy, not sweet. Proper Sunday vibes ☕",
    "imageUrl":     "https://res.cloudinary.com/di17ten5d/image/upload/v1775559036/filter_kaapi_e4gxxs.png",
    "tag":          "LOCAL",
    "latitude":     12.9784,
    "longitude":    77.6408,
    "upvoteCount":  42,
    "downvoteCount": 2,
    "commentCount": 12,
    "isVerified":   true,
    "createdAt":    "2026-04-16T10:30:00"
  },
  {
    "id":           "f7a3c9e1-22b4-4f8d-ae12-987654321def",
    "userId":       "a1b2c3d4-0002-4000-8000-aabbccddeeff",
    "userName":     "SilkBoard-Survivor",
    "content":      "ALERT: Huge water logging near Silk Board junction after that 20-min rain. Traffic is literally not moving towards HSR. Find an alternate route madi!",
    "imageUrl":     "https://res.cloudinary.com/di17ten5d/image/upload/v1775559037/signal_qamzzm.png",
    "tag":          "ALERT",
    "latitude":     12.9174,
    "longitude":    77.6229,
    "upvoteCount":  210,
    "downvoteCount": 5,
    "commentCount": 54,
    "isVerified":   true,
    "createdAt":    "2026-04-16T10:16:00"
  }
]
```

### Response Field Reference

| Field          | Type              | Nullable | Description                                  |
|----------------|-------------------|----------|----------------------------------------------|
| `id`           | `UUID`            | No       | Server-generated unique ID of the post       |
| `userId`       | `UUID`            | No       | ID of the user who created the post          |
| `userName`     | `String`          | No       | Display name of the user                     |
| `content`      | `String`          | No       | Text body of the post                        |
| `imageUrl`     | `String`          | Yes      | URL to the post image — render conditionally |
| `tag`          | `String`          | Yes      | Category label — render conditionally        |
| `latitude`     | `Double`          | No       | Geographic latitude                          |
| `longitude`    | `Double`          | No       | Geographic longitude                         |
| `upvoteCount`  | `Integer`         | No       | Number of upvotes                            |
| `downvoteCount`| `Integer`         | No       | Number of downvotes                          |
| `commentCount` | `Integer`         | No       | Number of comments                           |
| `isVerified`   | `Boolean`         | No       | Show verified badge if `true`                |
| `createdAt`    | `LocalDateTime`   | No       | ISO 8601 timestamp — format for display      |

---

## 🏷️ Tag Reference

| Tag        | Suggested UI Treatment          |
|------------|---------------------------------|
| `LOCAL`    | Green badge                     |
| `ALERT`    | Red badge                       |
| `CAUTION`  | Orange/Yellow badge             |
| `TRENDING` | Purple/Blue badge               |
| `null`     | No badge — hide the tag element |

---

## ❌ Error Response

Both endpoints return this shape on failure:

```json
{
  "apiPath":     "uri=/api/posts",
  "errorCode":   "INTERNAL_SERVER_ERROR",
  "errorMessage": "Error occurred while fetching posts: ...",
  "errorTime":   "2026-04-16T10:30:00"
}
```

| Field          | Type     | Description                         |
|----------------|----------|-------------------------------------|
| `apiPath`      | `String` | The endpoint that failed            |
| `errorCode`    | `String` | HTTP status description             |
| `errorMessage` | `String` | Human-readable error detail         |
| `errorTime`    | `String` | ISO 8601 timestamp of the error     |

---

## 🔌 Integration Notes for FE

- **CORS:** Use Next.js `rewrites` in `next.config.js` to proxy `/api/**` → `http://localhost:8080/api/**`. No direct browser calls needed.
- **`imageUrl`:** Always render conditionally — field is nullable.
- **`tag`:** Always render conditionally — field is nullable. Use the Tag Reference table above for badge styling.
- **`createdAt`:** Comes as ISO 8601 (`2026-04-16T10:30:00`) — use `dayjs` or `date-fns` to format as relative time (e.g. "2m ago").
- **`isVerified`:** Show a verified checkmark/badge when `true`.
- **`latitude` / `longitude`:** Available for map rendering if needed.
