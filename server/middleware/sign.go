package middleware

import (
	"crypto/md5"
	"encoding/hex"
	"fmt"
	"github.com/gin-gonic/gin"
	"github.com/flipped-aurora/gin-vue-admin/server/model/common/response"
	"sort"
	"strings"
	"time"
)

func SignVerify() gin.HandlerFunc {
	return func(c *gin.Context) {
		access := c.GetHeader("X-Access")
		timestamp := c.GetHeader("X-Timestamp")
		randstr := c.GetHeader("X-Randstr")
		sign := c.GetHeader("X-Sign")
		appKey := "your_app_key" // TODO: 从配置文件获取

		if access == "" || timestamp == "" || randstr == "" || sign == "" {
			response.FailWithMessage("缺少签名参数", c)
			c.Abort()
			return
		}

		// 验证时间戳是否在10分钟内
		ts, err := time.Parse("20060102150405", timestamp)
		if err != nil || time.Since(ts).Minutes() > 10 {
			response.FailWithMessage("时间戳无效或已过期", c)
			c.Abort()
			return
		}

		// 按ASCII排序参数
		params := make(map[string]string)
		params["access"] = access
		params["timestamp"] = timestamp
		params["randstr"] = randstr

		var keys []string
		for k := range params {
			keys = append(keys, k)
		}
		sort.Strings(keys)

		// 构建签名字符串
		var signStr string
		for _, k := range keys {
			signStr += fmt.Sprintf("%s=%s&", k, params[k])
		}
		signStr = strings.TrimRight(signStr, "&")
		signStr += fmt.Sprintf("&key=%s", appKey)

		// MD5加密
		h := md5.New()
		h.Write([]byte(signStr))
		calculatedSign := strings.ToUpper(hex.EncodeToString(h.Sum(nil)))

		if calculatedSign != sign {
			response.FailWithMessage("签名验证失败", c)
			c.Abort()
			return
		}

		c.Next()
	}
}
