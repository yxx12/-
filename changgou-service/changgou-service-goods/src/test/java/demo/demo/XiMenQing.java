package demo.demo;

public class XiMenQing {
    public static void main(String[] args) {
        JiaShi jiaShi=new JiaShi();
        WangPo wangPo=new WangPo(jiaShi);

        wangPo.makeEyesWithMan();
        wangPo.happyWithMan();
    }
}
