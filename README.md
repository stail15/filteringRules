# filteringRules
  This application is used to provide filtering process for input XML file.
   The result of execution is transformed into different sources - XML and temporary HTML files.

   The application is started from the command line with two parameters in the following form:
     >java -jar [path\to\file\]FilteringRules.jar arg[0] arg[1] 
    where:
    arg[0] - path to input XML file;
    arg[1] - path to output XML file*;
* - if file, specified in arg[1], does not exist, the application will attempt to create it.


   The input file should be present in the following format: 

<rules>
<rule name="a" type="child" weight="17"/>
<rule name="a" type="root" weight="29"/>
<rule name="b" type="sub" weight="56"/>
......
</rules>

   As shown above, each rule element in file has three attributes:
"name" - the rule name (not unique in input, but should be unique in output);
"type" - rule type can be one of 3 values: root, sub and child. The child rule is the most important; 
         the sub is of average importance and then the root is the least important.
"weight" - the weight specifies the rule importance within same type. The greater the weight value is,
           the more important the rule is. Weight is a positive integer 
           
           
   The Filtering process:
- application checks the rule type:
- for the same name, the child rule takes precedence over the sub rule and
  the sub rule over the root rule correspondingly.
- for the same rule, name and type, checks the rule weight. The rule with greater weight wins.

   When filtering process is finished, the result of it is transformed into XML and saved into output file,
specified in the input argument.
   Then application loads XSLT file from resources and applies it to the XML file. 
   The result of this transformation is stored in temporary HTML file and will be shown in the default browser,
if current JVM supports this action.
