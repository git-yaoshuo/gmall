package com.yaoshuo.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yaoshuo.gmall.bean.manage.BaseSaleAttr;
import com.yaoshuo.gmall.bean.manage.SpuImage;
import com.yaoshuo.gmall.bean.manage.SpuInfo;
import com.yaoshuo.gmall.bean.manage.SpuSaleAttr;
import com.yaoshuo.gmall.service.manage.SpuManageService;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin
public class SpuManageController {

    @Value("${fastdfs.storage.serverUrl}")
    private String serverUrl;

    @Reference
    private SpuManageService spuManageService;

    /**
     * 根据商品三级分类catalog3Id获取商品spuInfo列表
     * //http://localhost:8082/spuList?catalog3Id=64
     * @param catalog3Id
     * @return
     */
    @RequestMapping("/spuList")
    public List<SpuInfo> getSpuList(String catalog3Id) {
        return spuManageService.getSpuList(catalog3Id);
    }



    /**
     * 获取基本商品销售属性列表
     * //http://localhost:8082/baseSaleAttrList
     * @return
     */
    @RequestMapping("/baseSaleAttrList")
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return spuManageService.getBaseSaleAttrList();
    }



    /**
     * 上传商品图片
     *  //http://localhost:8082/fileUpload
     * @param file
     * @return
     */
    @RequestMapping("/fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile file) {

        try {

            /*String imgUrl=serverUrl;
            if(file!=null){
                System.out.println("multipartFile = " + file.getName()+"|"+file.getSize());
                String configFile = this.getClass().getResource("/tracker.conf").getFile();
                ClientGlobal.init(configFile);
                TrackerClient trackerClient=new TrackerClient();
                TrackerServer trackerServer=trackerClient.getTrackerServer();
                StorageClient storageClient=new StorageClient(trackerServer,null);
                String filename= file.getOriginalFilename();
                String extName = StringUtils.substringAfterLast(filename, ".");

                String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
                imgUrl=serverUrl ;
                for (int i = 0; i < upload_file.length; i++) {
                    String path = upload_file[i];
                    imgUrl+="/"+path;
                }

            }

            return imgUrl;*/

            StringBuilder imageUrl = new StringBuilder(serverUrl);

            if (file != null) {
                String configFile = this.getClass().getResource("/tracker.conf").getFile();
                ClientGlobal.init(configFile);
                TrackerClient trackerClient = new TrackerClient();
                TrackerServer trackerServer = trackerClient.getTrackerServer();
                StorageClient storageClient = new StorageClient(trackerServer, null);

                //String orginalFilename="D://1.jpg";

                String originalFilename = file.getOriginalFilename();
                String fileExtName = StringUtils.substringAfterLast(originalFilename, ".");

                String[] upload_file = storageClient.upload_file(file.getBytes(), fileExtName, null);

                if (upload_file != null && upload_file.length > 0) {
                    for (String path : upload_file) {
                        //http://192.168.80.131/group1/M00/00/00/wKhQg1_F9M6AWX39AACbuutfIxI294.jpg
                        imageUrl.append("/").append(path);
                        System.err.println("path = " + path);
                    }
                }
            }

            return imageUrl.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    /**
     * 保存商品spuInfo到数据库
     * //http://localhost:8082/saveSpuInfo
     * @param spuInfo
     */
    @RequestMapping("/saveSpuInfo")
    public void saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        spuManageService.saveSpuInfo(spuInfo);
    }



    /**
     * 根据spuId获取商品销售属性列表
     * //http://localhost:8082/spuSaleAttrList?spuId=58
     * @param spuId
     * @return
     */
    @RequestMapping("/spuSaleAttrList")
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuManageService.getSpuSaleAttrList(spuId);
    }



    /**
     * 根据商品spuId获取商品图片列表
     * //http://localhost:8082/spuImageList?spuId=58
     * @param spuId
     * @return
     */
    @RequestMapping("/spuImageList")
    public List<SpuImage> getSpuImageList(String spuId) {
        return spuManageService.getSpuImageList(spuId);
    }



}
