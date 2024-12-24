package movie

import (
	"github.com/flipped-aurora/gin-vue-admin/server/api/v1"
	"github.com/flipped-aurora/gin-vue-admin/server/middleware"
	"github.com/gin-gonic/gin"
)

type RouterGroup struct{}

func (r *RouterGroup) InitMovieRouter(Router *gin.RouterGroup) {
	movieRouter := Router.Group("movie").Use(middleware.SignVerify())
	movieApi := v1.ApiGroupApp.MovieApiGroup
	{
		movieRouter.POST("createOrder", movieApi.CreateOrder)       // 创建订单
		movieRouter.POST("getOrderList", movieApi.GetOrderList)     // 获取订单列表
		movieRouter.GET("getFinanceInfo", movieApi.GetFinanceInfo) // 获取财务信息
	}
}
