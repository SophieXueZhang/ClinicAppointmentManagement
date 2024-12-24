package movie

import (
	"github.com/flipped-aurora/gin-vue-admin/server/global"
)

type Movie struct {
	global.GVA_MODEL
	FilmID       int64   `json:"film_id" gorm:"index;comment:电影ID"`
	FilmName     string  `json:"film_name" gorm:"comment:电影名称"`
	FilmPic      string  `json:"film_pic" gorm:"comment:电影海报"`
	Duration     int     `json:"duration" gorm:"comment:电影时长(分钟)"`
	Language     string  `json:"language" gorm:"comment:语言"`
	PlanType     string  `json:"plan_type" gorm:"comment:放映类型(2D/3D)"`
	OriginalPrice string `json:"original_price" gorm:"comment:原价"`
	SettlePrice   string `json:"settle_price" gorm:"comment:结算价"`
}

type MovieOrder struct {
	global.GVA_MODEL
	OrderSN        string  `json:"order_sn" gorm:"index;comment:订单号"`
	ThirdOrderSN   string  `json:"third_order_sn" gorm:"index;comment:第三方订单号"`
	FilmID         int64   `json:"film_id" gorm:"index;comment:电影ID"`
	FilmName       string  `json:"film_name" gorm:"comment:电影名称"`
	CinemaID       int64   `json:"cinema_id" gorm:"comment:影院ID"`
	CinemaName     string  `json:"cinema_name" gorm:"comment:影院名称"`
	ShowTime       string  `json:"show_time" gorm:"comment:放映时间"`
	HallName       string  `json:"hall_name" gorm:"comment:影厅名称"`
	SeatNo         string  `json:"seat_no" gorm:"comment:座位号"`
	OriginalFee    string  `json:"original_fee" gorm:"comment:原价"`
	TotalFee       string  `json:"total_fee" gorm:"comment:总价"`
	PayStatus      int     `json:"pay_status" gorm:"comment:支付状态 2=已支付 -2=已退款"`
	PayTime        int64   `json:"pay_time" gorm:"comment:支付时间"`
	Status         int     `json:"status" gorm:"comment:订单状态 0=已创建 4=已推 5=出票中 6=已出票 7=更新出票 8=已完成 -1=已取消 -2=已关闭"`
	FastBuy        int     `json:"fast_buy" gorm:"comment:是否快速出票 0=特惠出票 1=快速出票"`
	ReservedPhone  string  `json:"reserved_phone" gorm:"comment:预留手机号"`
	TicketCode     string  `json:"ticket_code" gorm:"comment:取票码"`
}

type Finance struct {
	global.GVA_MODEL
	PubID        int64   `json:"pub_id" gorm:"index;comment:代理商ID"`
	Balance      string  `json:"balance" gorm:"comment:余额总额"`
	FreezeMoney  string  `json:"freeze_money" gorm:"comment:冻结金总额"`
	Profit       string  `json:"profit" gorm:"comment:佣金总额"`
}

func (Movie) TableName() string {
	return "movies"
}

func (MovieOrder) TableName() string {
	return "movie_orders"
}

func (Finance) TableName() string {
	return "finances"
}
