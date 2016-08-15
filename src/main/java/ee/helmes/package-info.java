/**
 *This application is used to provide filtering process for input XML file.<br>
 *The result of execution is transformed into different sources - XML and temporary HTML files.<br>
 *
 *The application is started from the command line with two parameters in the following form:<br>
 *<b>{@code >java -jar [path\to\file\]FilteringRules.jar arg[0] arg[1]}</b><br>
 *where:<br>
 *{@code arg[0]} - path to input XML file;<br>
 *{@code arg[1]} - path to output XML file*;<br>
 ** - if file specified in {@code arg[1]} does not exist, the application will attempt to create it.<br>
 *The input file should be present in the following format:<br>
 *{@code<rules>}<br>
 *{@code <rule name="a" type="child" weight="17"/>}<br>
 *{@code <rule name="a" type="root" weight="29"/>}<br>
 *{@code <rule name="b" type="sub" weight="56"/>}<br>
 *{@code  ......}<br>
 *{@code </rules>}<br>

 *As shown above, each rule element in file has three attributes:<br>
 *"name" - the rule name (not unique in input, but should be unique in output);<br>
 * "type" - rule type can be one of 3 values: root, sub and child.
 * The child rule is the most important; the sub is of average importance
 * and then the root is the least important.<br>
 * "weight" - the weight specifies the rule importance within same type.
 * The greater the weight value is, the more important the rule is.
 * Weight is a positive integer.<br>
 *<br>
 * The Filtering process:<br>
 * -  application checks the rule type:<br>
 * -  for the same name, the child rule takes precedence over the sub rule
 * and the sub rule over the root rule correspondingly.<br>
 * - for the same rule, name and type, checks the rule weight.
 * The rule with greater weight wins.<br>
 *<br>
 * When filtering process is finished, the result of it is transformed
 * into XML and saved into output file, specified in the input argument.<br>
 *Then application loads XSLT file from resources and applies it to the XML file.
 * The result of this transformation is stored in temporary HTML file and will be shown in the default browser,
 * if current JVM supports this action.
 */
package ee.helmes;