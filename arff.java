package Snowball;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;


/**
 * Created by Hedy on 2017/4/26.
 */
public class arff {

    public static void main(String[] args ) throws Exception{
        int index = 0;
        String[] tag = new String[9];
        tag[0]="0.85_LO";
        tag[1]="0.84_ME_1";
        tag[2] = "0.84_MA";
        tag[3] = "0.84_HO";
        tag[4] = "0.79_JO";
        tag[5] = "0.75_TL";
        tag[6] = "0.75_PR";
        tag[7] = "0.8_AC";
        tag[8] = "0.84_HE";

        String filename ="F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag[index]+"\\Feature.txt";
        String arfffile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag[index]+"\\data.arff";

        FastVector atts;
        FastVector attsRel;
        FastVector attVals;
        FastVector attValsRel;
        Instances data;
        //Instances dataRel;
        double[] vals;
        double[] valsRel;
        int i ;

        //set up attributes
        atts = new FastVector();
        //属性1是pattern抽取到的tuple的频率分布
        atts.addElement(new Attribute("att1"));
        //属性2是互斥的聚类个数
        atts.addElement(new Attribute("att2"));
        atts.addElement(new Attribute("att3",(FastVector)null));
        atts.addElement(new Attribute("att4","yyyy-MM-dd"));
        attsRel = new FastVector();
        attsRel.addElement(new Attribute("att5.1"));
        attValsRel = new FastVector();
        for (i = 0; i < 5; i++){
            attValsRel.addElement("val5."+(i+1));
        }
        attsRel.addElement(new Attribute("att5.2",attValsRel));
        //dataRel = new Instances("att5",attsRel,0);
        //atts.addElement(new Attribute("att5", dataRel, 0));

        data = new Instances("MyRelation",atts,0);
        CreateInstance(filename,data);
        //fill with data
        vals = new double[data.numAttributes()];
        vals[0] = Math.PI;
        vals[1] = attVals.indexOf("val3");
        vals[2] = data.attribute(2).addStringValue("This is a string!");
        vals[3] = data.attribute(3).parseDate("2001-11-09");
        //dataRel = new Instances(data.attribute(4).relation(), 0);
        // -- first instance  
        valsRel = new double[2];
        valsRel[0] = Math.PI + 1;
        valsRel[1] = attValsRel.indexOf("val5.3");
        //dataRel.add(new Instance(1.0, valsRel));
        // -- second instance  
        valsRel = new double[2];
        valsRel[0] = Math.PI+2;
        valsRel[1] = attValsRel.indexOf("val5.2");
        //dataRel.add(new Instance(1.0,valsRel));
        //vals[4] = data.attribute(4).addRelation(dataRel);
        // add  
        data.add(new Instance(1.0,vals));

        // second instance  
        vals = new double[data.numAttributes()];// important: needs NEW array!  
        // - numeric  
        vals[0] = Math.E;
        // - nominal  
        vals[1] = attVals.indexOf("val1");
        // - string  
        vals[2] = data.attribute(2).addStringValue("And another one!");
        // - date  
        vals[3] = data.attribute(3).parseDate("2000-12-01");
        // - relational  
        //dataRel = new Instances(data.attribute(4).relation(),0);
        // -- first instance  
        valsRel = new double[2];
        valsRel[0] = Math.E+1;
        valsRel[1] = attValsRel.indexOf("val5.4");
        //dataRel.add(new Instance(1.0, valsRel));
        // -- second instance  
        valsRel = new double[2];
        valsRel[0] = Math.E+2;
        valsRel[1] = attValsRel.indexOf("val5.1");
        //dataRel.add(new Instance(1.0,valsRel));
        //vals[4] = data.attribute(4).addRelation(dataRel);
        // add  
        data.add(new Instance(1.0,vals));
        // 4. output data  
        System.out.println(data);

    }

}
