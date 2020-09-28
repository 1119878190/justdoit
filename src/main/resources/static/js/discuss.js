$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

//点赞
function like(btn,entityType,entityId,entityUserId,postId) {

    $.post({
        url: "/like",
        data: {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        success:function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':'赞');
            }else {
                alert(data.msg);
            }
        }
    });
}


//置顶
function setTop() {
    var id = $("#postId").val();
    $.post({
        url: "/discuss/top",
        data: {"id":id},
        success:function (data) {
             data = $.parseJSON(data);
            if (data.code == 0){
                //置顶成功后,将隐藏置顶按钮
                $("#topBtn").attr("disabled","disabled");
            }else {
                alert(data.msg);
            }
        }
    })
}

//加精
function setWonderful() {
    var id = $("#postId").val();
    $.post({
        url: "/discuss/wonderful",
        data: {"id":id},
        success:function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                //加精成功后,将隐藏置顶按钮
                $("#wonderfulBtn").attr("disabled","disabled");
            }else {
                alert(data.msg);
            }
        }
    });
}

//删除
function setDelete() {
    var id = $("#postId").val();
    $.post({
        url: "/discuss/delete",
        data: {"id":id},
        success:function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                //删除成功后,跳转主页
                location.href = "/index";
            }else {
                alert(data.msg);
            }
        }
    });
}