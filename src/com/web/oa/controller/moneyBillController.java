package com.web.oa.controller;

import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.service.MoneyBillService;
import com.web.oa.utils.Constants;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class moneyBillController {
    @Autowired
    private MoneyBillService moneyBillService;
    /*
     * 显示我的报销单列表
     */
    @RequestMapping("/myBaoxiaoBill")
    public String myBaoxiaoBill(ModelMap model){
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        List<BaoxiaoBill> list = moneyBillService.findLeaveBillListByUser(activeUser.getId());
        //放置到上下文对象中
        model.addAttribute("baoxiaoList", list);
        return "baoxiaobill";
    }

    /*
    * 显示我的代办事务
    */
    @RequestMapping("/myTaskList")
    public String myTaskList(Model model){
        //获取当前对象
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        //查询我的任务
        List<Task> myTaskList = moneyBillService.findTaskListByName(activeUser.getUsername());
        model.addAttribute("taskList", myTaskList);
        return "workflow_task";
    }
    /*
     * 显示流程信息
     */
    @RequestMapping("/processDefinitionList")
    public String processDefinitionList(Model model){
        //查询流程部署的名称等信息
        List<Deployment> deployments = moneyBillService.findDeploymentList();
        //查询流程定义信息
        List<ProcessDefinition> definitions = moneyBillService.findDefinitionList();
        model.addAttribute("depList", deployments);
        model.addAttribute("pdList", definitions);
        return "workflow_list";
    }


    /*
     *  部署流程
     */
    @RequestMapping("/deployProcess")
    public String deployProcess(String processName,MultipartFile fileName){
        try {
            moneyBillService.deployProcess(processName,fileName.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/processDefinitionList";
    }

    /*
     * 启动任务
     */
    @RequestMapping("/saveStartBaoxiao")
    public String saveStartBaoxiao(BaoxiaoBill baoxiaoBill) {
        //设置当前时间
        baoxiaoBill.setCreatdate(new Date());
        //获取当前用户对象，设置申请人ID
        //Employee employee = (Employee) session.getAttribute(Constants.GLOBLE_USER_SESSION);
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        baoxiaoBill.setUserId(activeUser.getId());
        //更新状态从0变成1
        baoxiaoBill.setState(1);
        //存放报销单信息,同时会将报销单数据库自增的id封装在对象中
        moneyBillService.saveBaoxiao(baoxiaoBill);
        //启动任务，
        moneyBillService.saveStartProcess(baoxiaoBill.getId(), activeUser.getUsername());

        return "redirect:/myTaskList";
    }
    /*
     *  办理任务页面
     */
    @RequestMapping("/viewTaskForm")
    public String viewTaskForm(String taskId,Model model){
        //查询当前的报销单信息
        BaoxiaoBill bill = this.moneyBillService.findBaoxiaoBillByTaskId(taskId);
        //查询所有批注信息
        List<Comment> list = this.moneyBillService.findCommentByTaskId(taskId);
        //查询连线情况(之后要控制走向)
        List<String> outcomeList = this.moneyBillService.findOutComeListByTaskId(taskId);
        model.addAttribute("baoxiaoBill", bill);
        model.addAttribute("commentList", list);
        model.addAttribute("outcomeList", outcomeList);
        model.addAttribute("taskId", taskId);
        return "approve_baoxiao";
    }

    /**
     * 提交任务，推给下一个办理人
     * @param id 报销单的id
     * @param taskId 任务id
     * @param comment 批注
     * @return workflow_task.jsp
     */
    @RequestMapping("/submitTask")
    public String submitTask(Long id,String taskId,String comment,String outcome){
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        String username = activeUser.getUsername();
        this.moneyBillService.submitTask(id,taskId,comment,username,outcome);
        return "redirect:/myTaskList";
    }

    /**
     * 查询历史批注信息
     */
    @RequestMapping("/viewHisComment")
    public String viewHisComment(Long id,Model model){
        //根据报销单id查报销单信息
        BaoxiaoBill baoxiaoBill = moneyBillService.findBaoxiaoBillById(id);
        //根据报销单查询历史批注信息
        List<Comment> commentList = moneyBillService.findCommentsByBillId(id);
        model.addAttribute("baoxiaoBill", baoxiaoBill);
        model.addAttribute("commentList", commentList);
        return "workflow_commentlist";
    }

    /**
     * 删除部署信息
     */
    @RequestMapping("/delDeployment")
    public String delDeployment(String deploymentId){
        //使用部署对象ID，删除流程定义
        this.moneyBillService.deleteProcessDefinitionByDeploymentId(deploymentId);
        return "redirect:/processDefinitionList";
    }

    /**
     * 删除报销单
     */
    @RequestMapping("/baoXiaoBillAction_delete")
    public String delLeaveBill(Long id){
        moneyBillService.deleteBaoXiaoBill(id);
        return "redirect:/myBaoxiaoBill";
    }

    /**
     * 查看我的报销单里的当前流程图
     */
    @RequestMapping("/viewCurrentImageByBill")
    public String viewCurrentImageByBill(long billId,Model model){
        String BUSSINESS_KEY = Constants.BAOXIAO_KEY + "." + billId;
        Task task = this.moneyBillService.findTaskByBussinessKey(BUSSINESS_KEY);
        /*一：查看流程图*/
        //1：获取任务ID，获取任务对象，使用任务对象获取流程定义ID，查询流程定义对象
        ProcessDefinition pd = moneyBillService.findProcessDefinitionByTaskId(task.getId());

        model.addAttribute("deploymentId", pd.getDeploymentId());
        model.addAttribute("imageName", pd.getDiagramResourceName());
        /*：查看当前活动，获取当期活动对应的坐标x,y,width,height，将4个值存放到Map<String,Object>中*/
        Map<String, Object> map = moneyBillService.findCoordingByTask(task.getId());

        model.addAttribute("acs", map);
        return "viewimage";
    }


    /**
     * 查看我的代办事务里的当前流程图
     */
    @RequestMapping("/viewCurrentImage")
    public String viewCurrentImage(String taskId,ModelMap model){
        /*一：查看流程图*/
        //1：获取任务ID，获取任务对象，使用任务对象获取流程定义ID，查询流程定义对象
        ProcessDefinition pd = moneyBillService.findProcessDefinitionByTaskId(taskId);

        model.addAttribute("deploymentId", pd.getDeploymentId());
        model.addAttribute("imageName", pd.getDiagramResourceName());
        /*二：查看当前活动，获取当期活动对应的坐标x,y,width,height，将4个值存放到Map<String,Object>中*/
        Map<String, Object> map = moneyBillService.findCoordingByTask(taskId);

        model.addAttribute("acs", map);
        return "viewimage";
    }

    /**
     * 查看流程管理里的流程定义图
     */
    @RequestMapping("/viewImage")
    public void viewImage(String deploymentId, String imageName, HttpServletResponse response) throws Exception{

        //2：获取资源文件表（act_ge_bytearray）中资源图片输入流InputStream
        InputStream in = moneyBillService.findImageInputStream(deploymentId,imageName);
        //3：从response对象获取输出流
        OutputStream out = response.getOutputStream();
        //4：将输入流中的数据读取出来，写到输出流中
        for(int b=-1;(b=in.read())!=-1;){
            out.write(b);
        }
        out.close();
        in.close();
    }

}
