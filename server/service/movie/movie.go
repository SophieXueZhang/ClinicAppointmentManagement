package movie

import (
	"github.com/flipped-aurora/gin-vue-admin/server/global"
	"github.com/flipped-aurora/gin-vue-admin/server/model/movie"
	"github.com/flipped-aurora/gin-vue-admin/server/model/common/request"
)

type MovieService struct{}

func (m *MovieService) GetOrderList(info request.PageInfo) (list interface{}, total int64, err error) {
	limit := info.PageSize
	offset := info.PageSize * (info.Page - 1)
	db := global.GVA_DB.Model(&movie.MovieOrder{})
	var orders []movie.MovieOrder
	err = db.Count(&total).Error
	if err != nil {
		return
	}
	err = db.Limit(limit).Offset(offset).Find(&orders).Error
	return orders, total, err
}
