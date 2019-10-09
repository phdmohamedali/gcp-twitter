#
SELECT
  created_at,
  text,
  id,
  retweeted_status.retweet_count,
  user.screen_name
FROM
  `Twitter.tweets` 
WHERE
  LOWER(text) LIKE '%kubernetes%'
  OR LOWER(text) LIKE '%bigquery%'
  OR LOWER(text) LIKE '%redis%'
ORDER BY
  created_at DESC
LIMIT
  1000
