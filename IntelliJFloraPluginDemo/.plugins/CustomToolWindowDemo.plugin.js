importPackage(com.intellij.util.ui.components)
importPackage(com.intellij.ui.components)

const panel = new BorderLayoutPanel()
const html = "<html><body>" +
    "<h1>Microbe?</h1>" +
    "<img src='https://cdn.factcheck.org/UploadedFiles/NovelCoronavirus-355x355.jpg' />" +
    "<h1>.. no. It is a Virus</h1>" +
    "</body></html>"

panel.addToCenter(new JBLabel(html))

const id = "Microbe"

ide.registerToolWindow(
    id,
    ToolWindowAnchor.RIGHT,
    panel,
    false,
    true,
    true,
    true,
    null,
    null,
    null
)