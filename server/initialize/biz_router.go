package initialize

import (
	"github.com/flipped-aurora/gin-vue-admin/server/router"
	"github.com/gin-gonic/gin"
)

func initBizRouter(PrivateGroup *gin.RouterGroup, PublicGroup *gin.RouterGroup) {
	movieRouter := router.RouterGroupApp.Movie
	movieRouter.InitMovieRouter(PublicGroup)
}
