//发送私信
$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	//关闭发送框
	$("#sendModal").modal("hide");

	//发送数据 异步
	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();

	$.post({
		url:"/letter/send",
		data:{"toName":toName,"content":content},
		success:function (data) {
			//将json转换为js对象
			data = $.parseJSON(data);
			if (data.code == 0){
				$("#hintBody").text("发送成功");
			}else {
				$("#hintBody").text(data.msg);
			}


			//显示提示框
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//刷新页面
				location.reload();
			}, 2000);

		}
	});


}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}