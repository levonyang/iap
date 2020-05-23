package com.haizhi.iap.follow.controller;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.haizhi.iap.common.Wrapper;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Api(tags="【关注-上传模块】手动上传图片截图")
@Slf4j
@RestController
@RequestMapping(value = "/follow/upload")
public class UploadController {

    @Setter
    @Value(value = "${ip.upload_img_view}")
    String uploadImgView;
	
	 @RequestMapping(value = "/uploadImg", method = RequestMethod.POST,
	            consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
	    public Wrapper uploadImg(@RequestParam(value = "img", required = false) MultipartFile img) throws IllegalStateException, IOException {
		      JSONObject result = new JSONObject();
		      boolean flag = true;
		      upload(img, result);
	         return Wrapper.OKBuilder.data(result).build();
	    }

	   
	 
	 /**
	  * 上传图片
	  * @param file
	  * @param params
	  * @return
	  * @throws Exception
	  */
	 public   boolean upload(MultipartFile file, JSONObject params) {
	     //过滤合法的文件类型
	     try {
	    	 String fileName = file.getOriginalFilename();
		     String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
		     String allowSuffixs = "gif,jpg,jpeg,bmp,png,ico";
		     if(allowSuffixs.indexOf(suffix) == -1){
		    	 params.put("code", "-1");
		         params.put("message", "不支持的图片类型");
		         return false;
		     }
		     
		     if(file.getSize()>3*1024*1024){
		    	 //文件大小大于3M不能上传
		    	 params.put("code", "-2");
		    	 params.put("message", "图片过大");
		         return false;
		     }
		     //创建新目录
		     String datedir = getNowDateStr();
		     String path=System.getProperty("evan.follow")+File.separator+"viewImg" +File.separator+ datedir;
		     log.info("图片上传相关路径：evan.follow"+System.getProperty("evan.follow")+" path:"+path);
		     File dir = new File(path);
		     if(!dir.exists()){
		         dir.mkdirs();
		     }
		     
		     //创建新文件
		     String newFileName = UUID.randomUUID().toString().replace("-", "");
		     File f = new File(dir.getPath() + File.separator + newFileName + "." + suffix);
		     
		     //将输入流中的数据复制到新文件
		     FileUtils.copyInputStreamToFile(file.getInputStream(), f);
		     String filepath=uploadImgView+path+File.separator+newFileName;
		     params.put("code", "1");
	         params.put("message", "上传成功");
	         params.put("filepath", filepath);
	         log.info("图片上传全路径:"+filepath);
		} catch (Exception e) {
			 params.put("code", "-3");
	         params.put("message", "上传失败");
	         log.error("图片上传失败:"+e.getMessage());
		}
	     return true;
	 }

	 
	 
	 /**
	  * 获取当前日期字符串
	  * @param separator
	  * @return
	  */
	 public static String getNowDateStr(){
	     Calendar now = Calendar.getInstance();
	     int year = now.get(Calendar.YEAR);
	     int month = now.get(Calendar.MONTH)+1;
	     int day = now.get(Calendar.DATE);
	     
	     return year + "" + (month<10 ?"0"+month:month)  + (day<10 ? "0"+day:day);
	 }
	 
	 public static void main(String[] args) {
		 JSONObject params = new JSONObject();
		 params.put("code", "1");
    	 params.put("message", "上传成功");
    	 params.put("filepath", "Ip/路径1/路径2/文件名");
		 Wrapper.OKBuilder.data(params).build();
		 System.out.println(params);
		 
	 }
	 

}
