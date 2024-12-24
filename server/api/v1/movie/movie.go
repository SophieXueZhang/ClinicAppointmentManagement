package movie

import (
	"github.com/flipped-aurora/gin-vue-admin/server/global"
	"github.com/flipped-aurora/gin-vue-admin/server/model/common/response"
	"github.com/flipped-aurora/gin-vue-admin/server/model/movie"
	"github.com/flipped-aurora/gin-vue-admin/server/model/common/request"
	"github.com/flipped-aurora/gin-vue-admin/server/utils"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

type MovieApi struct{}

var movieService = new(movie.MovieService)

// CreateOrder 创建订单
func (m *MovieApi) CreateOrder(c *gin.Context) {
	var order movie.MovieOrder
	err := c.ShouldBindJSON(&order)
	if err != nil {
		response.FailWithMessage(err.Error(), c)
		return
	}
	
	if err := global.GVA_DB.Create(&order).Error; err != nil {
		global.GVA_LOG.Error("创建订单失败!", zap.Error(err))
		response.FailWithMessage("创建订单失败", c)
		return
	}
	response.OkWithMessage("创建订单成功", c)
}

// GetOrderList 获取订单列表
func (m *MovieApi) GetOrderList(c *gin.Context) {
	var pageInfo request.PageInfo
	err := c.ShouldBindJSON(&pageInfo)
	if err != nil {
		response.FailWithMessage(err.Error(), c)
		return
	}
	
	list, total, err := movieService.GetOrderList(pageInfo)
	if err != nil {
		global.GVA_LOG.Error("获取订单列表失败!", zap.Error(err))
		response.FailWithMessage("获取订单列表失败", c)
		return
	}
	response.OkWithDetailed(response.PageResult{
		List:     list,
		Total:    total,
		Page:     pageInfo.Page,
		PageSize: pageInfo.PageSize,
	}, "获取订单列表成功", c)
}

// GetFinanceInfo 获取财务信息
func (m *MovieApi) GetFinanceInfo(c *gin.Context) {
	pubID := utils.GetUserID(c)
	var finance movie.Finance
	
	err := global.GVA_DB.Where("pub_id = ?", pubID).First(&finance).Error
	if err != nil {
		global.GVA_LOG.Error("获取财务信息失败!", zap.Error(err))
		response.FailWithMessage("获取财务信息失败", c)
		return
	}
	response.OkWithData(finance, c)
}
