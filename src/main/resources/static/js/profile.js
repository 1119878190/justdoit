$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	if($(btn).hasClass("btn-info")) {
		// 关注TA   prev()获取当前节点的上一个节点
		$.post({
			url:"/follow",
			data:{"entityType":3,"entityId":$(btn).prev().val()},
			success:function (data) {
				data = $.parseJSON(data);
				if (data.code == 0){
					window.location.reload();
				}else {
					alert(data.msg);
				}
			}
		});

		//$(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
	} else {
		// 取消关注
		$.post({
			url:"/unfollow",
			data:{"entityType":3,"entityId":$(btn).prev().val()},
			success:function (data) {
				data = $.parseJSON(data);
				if (data.code == 0){
					window.location.reload();
				}else {
					alert(data.msg);
				}
			}
		});
		//$(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
	}
}