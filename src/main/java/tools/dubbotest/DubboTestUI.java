/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package tools.dubbotest;

import com.google.gson.Gson;
import tools.dubbotest.save.HistoryDataStore;
import tools.dubbotest.save.JarData;
import tools.dubbotest.save.MethodInfo;
import tools.dubbotest.util.LangHelper;
import tools.dubbotest.util.SystemHelper;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liukaixuan(liukaixuan@gmail.com)
 */
public class DubboTestUI extends JFrame {

	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DubboTestUI.class);

	final static int PADDING = 10;

	final static int WIDTH = 1024;

	ConcurrentHashMap<String, JComponent> inputComponts = new ConcurrentHashMap<>();

	ConcurrentHashMap<String, JComponent> inputParamsComponts = new ConcurrentHashMap<>();

	boolean resizeFlag = false;

	JPanel paramPanel;

	private String selectedApi;

	private HistoryDataStore dataStore;

	public DubboTestUI() {
		super();

		this.dataStore = HistoryDataStore.getInstance();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		screenSize.setSize(screenSize.getWidth() - 200, screenSize.getHeight() - 100);

		setSize(screenSize);
		setTitle("dubbo test ui");

		this.addWindowListener(new WindowAdapter() {

			@Override public void windowClosing(WindowEvent e) {
				saveToFile();
				DubboTestUI.this.dispose();

				System.exit(0);
			}
		});

		this.setupUI();
		this.setVisible(true);

		this.setLocationRelativeTo(null);
	}

	private void setupUI() {
		JPanel fixed = new JPanel();

		MyVFlowLayout headLayout = new MyVFlowLayout();
		headLayout.setVgap(PADDING);
		headLayout.setHgap(PADDING);
		fixed.setLayout(headLayout);

		this.paramPanel = new JPanel();
		MyVFlowLayout paramsLayout = new MyVFlowLayout();
		paramsLayout.setVgap(PADDING);
		paramsLayout.setHgap(PADDING);
		paramPanel.setLayout(paramsLayout);

		this.setLayout(new BorderLayout());
		this.add(BorderLayout.NORTH, fixed);
		this.add(BorderLayout.CENTER, paramPanel);

		//添加元素
		this.addFixRow(fixed);

		this.buildMenu();
	}

	private void buildMenu() {
		JMenu jm = new JMenu(LangHelper.getText("Help"));
		JMenuItem t1 = new JMenuItem(LangHelper.getText("Upgrade"));
		t1.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				SystemHelper.openBrowser("https://github.com/liukaixuan/DubboSwingTestTool");
			}
		});
		jm.add(t1);

		JMenuBar br = new JMenuBar();  //创建菜单工具栏
		br.add(jm);      //将菜单增加到菜单工具栏

		this.setJMenuBar(br);  //为 窗体设置  菜单工具栏
	}

	private void addFixRow(JPanel fixed) {
		JarData jarData = dataStore.getCurrentJarData();

		inputComponts.put("zookeeper",
				buildRow(fixed, LangHelper.getText("zookeeper"), dataStore.getCurrentZookeeper(), null));

		final JTextField jarLocationUI = (JTextField) buildRow(fixed, LangHelper.getText("jarLocation"),
				jarData.getJarLocation(), null);
		inputComponts.put("jarLocation", jarLocationUI);
		inputComponts.put("service", buildRow(fixed, LangHelper.getText("service"), jarData.getService(), null));
		inputComponts.put("group", buildRow(fixed, "group", jarData.getGroup(), null));
		inputComponts.put("version",
				buildRow(fixed, LangHelper.getText("serviceVersion"), jarData.getServiceVersion(), null));

		jarLocationUI.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) {

			}

			@Override public void removeUpdate(DocumentEvent e) {

			}

			@Override public void changedUpdate(DocumentEvent e) {
				String inputLocation = getComponetInputValue(jarLocationUI);

				if (inputLocation == null) {
					return;
				}

				JarData jar = dataStore.getHistoryData().getJarInfos().get(inputLocation);

				if (jar != null) {
					((JTextField) inputComponts.get("service")).setText(jar.getService());
					((JTextField) inputComponts.get("group")).setText(jar.getGroup());
					((JTextField) inputComponts.get("version")).setText(jar.getServiceVersion());
				}

			}
		});

		JPanel actionPanel = new JPanel();
		actionPanel.add(new JLabel("  "));
		JButton refreshButton = new JButton(LangHelper.getText("refreshMethods"));
		actionPanel.add(refreshButton);
		actionPanel.add(new JLabel("   "));

		JButton invokeButton = new JButton(LangHelper.getText("invokeMethod"));
		actionPanel.add(invokeButton);

		buildRow(fixed, LangHelper.getText("ops"), actionPanel);

		refreshButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {

				reBuildParamsUI(null);
			}
		});

		invokeButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {

				saveToFile();

				MethodInfo methodInfo = getCurrentSelectedMethodInfo();

				ApiInstance api = getApiInstanceFromInput();
				if (api == null) {
					return;
				}

				try {
					Object obj = api.invokeMethod(selectedApi, methodInfo.getOrderedParams());

					showResult(obj);
				} catch (Exception e1) {
					log.error("failed to invoke selected method:" + selectedApi, e1);

					JOptionPane.showMessageDialog(null, LangHelper.getText("callApiFailed") + e1.getMessage(),
							LangHelper.getText("callServiceFailed"), JOptionPane.ERROR_MESSAGE);
				}
			}
		});

	}

	protected void saveToFile() {
		syncUIToJarInfo();
		syncUIParams();

		dataStore.storeToFile();
	}

	protected void showResult(Object result) {
		final String msg = result == null ? "null" : new Gson().toJson(result);

		final JDialog dialog = new JDialog();
		dialog.setSize(600, 400);
		dialog.setLocationRelativeTo(null);

		dialog.setModal(true);
		dialog.setTitle(LangHelper.getText("executeResult"));

		JTextArea area = new JTextArea();
		area.setLineWrap(true);
		area.setEditable(false);
		dialog.setLayout(new BorderLayout());
		dialog.add(BorderLayout.CENTER, area);

		area.setText(msg);

		JButton copyButton = new JButton(LangHelper.getText("copy"));
		copyButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				setSysClipboardText(msg);

				dialog.dispose();
			}
		});

		JButton closeButton = new JButton(LangHelper.getText("close"));
		closeButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});

		JPanel buttons = new JPanel();
		buttons.add(copyButton);
		buttons.add(new JLabel("  "));
		buttons.add(closeButton);

		dialog.add(BorderLayout.SOUTH, buttons);

		dialog.setVisible(true);
	}

	/**
	 * 将字符串复制到剪切板。
	 */
	public static void setSysClipboardText(String writeMe) {
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable tText = new StringSelection(writeMe);
		clip.setContents(tText, null);
	}

	private String getInputValue(String key) {
		JComponent component = inputComponts.get(key);

		return getComponetInputValue(component);
	}

	private ApiInstance getApiInstanceFromInput() {
		//String address, String group, String version, String className,String jarLocation
		try {
			ApiInstance instance = ApiInstance
					.getInstance(getInputValue("zookeeper"), getInputValue("group"), getInputValue("version"),
							getInputValue("service"), getInputValue("jarLocation"));

			return instance;
		} catch (Throwable throwable) {
			log.error("failed to init selected method:" + selectedApi, throwable);

			JOptionPane.showMessageDialog(null, LangHelper.getText("getApiFailed") + throwable.getMessage(),
					LangHelper.getText("getServiceFailed"), JOptionPane.ERROR_MESSAGE);
		}

		return null;
	}

	protected void syncUIToJarInfo() {
		this.dataStore.switchToJar(getComponetInputValue(inputComponts.get("jarLocation")));

		this.dataStore.setJarDataInfo(getComponetInputValue(inputComponts.get("zookeeper")),
				getComponetInputValue(inputComponts.get("service")), getComponetInputValue(inputComponts.get("group")),
				getComponetInputValue(inputComponts.get("version")));
	}

	protected String getComponetInputValue(JComponent component) {
		String value = null;

		if (component instanceof JComboBox) {
			value = (String) ((JComboBox) component).getSelectedItem();
		} else if (component instanceof JList) {
			value = (String) ((JList) component).getSelectedValue();
		} else {
			value = ((JTextField) component).getText();
		}

		if ("null".equals(value)) {
			value = null;
		}

		return value;
	}

	protected void syncUIParams() {
		MethodInfo methodInfo = getCurrentSelectedMethodInfo();

		if (methodInfo == null) {
			return;
		}

		int paramLength = methodInfo.getParams().size();

		for (int i = 0; i < paramLength; i++) {
			JComponent component = inputParamsComponts.get(i + "");
			String value = getComponetInputValue(component);

			methodInfo.getParams().put(i, value);
		}
	}

	protected MethodInfo getCurrentSelectedMethodInfo() {
		if (selectedApi == null) {
			return null;
		}

		ApiInstance api = getApiInstanceFromInput();
		if (api == null) {
			return null;
		}

		Method m = api.getApiMethods().get(selectedApi);

		return dataStore.getMethodInfo(dataStore.getCurrentJarData().getService(), m);
	}

	private void reBuildParamsUI(String selectedMethod) {
		if (selectedApi != null && selectedApi.equals(selectedMethod)) {
			return;
		}

		ApiInstance api = getApiInstanceFromInput();
		if (api == null) {
			return;
		}

		final java.util.List<String> ms = api.getApiMethodSignatures();
		if (ms.size() == 0) {
			return;
		}

		if (selectedMethod == null) {
			selectedMethod = ms.get(0);
		}

		if (selectedApi != null) {
			//保存老数据
			syncUIParams();
		}

		this.selectedApi = selectedMethod;

		inputParamsComponts.clear();
		paramPanel.removeAll();

		final JComponent selectedMethodUI = buildRow(paramPanel, LangHelper.getText("methods"), selectedMethod,
				ms.toArray(new String[0]));

		if (selectedMethodUI instanceof JComboBox) {
			((JComboBox) selectedMethodUI).addItemListener(new ItemListener() {
				@Override public void itemStateChanged(ItemEvent e) {
					int index = ((JComboBox) selectedMethodUI).getSelectedIndex();

					reBuildParamsUI(ms.get(index));
				}
			});
		} else if (selectedMethodUI instanceof JList) {
			((JList) selectedMethodUI).addListSelectionListener(new ListSelectionListener() {

				@Override public void valueChanged(ListSelectionEvent e) {
					int index = ((JList) selectedMethodUI).getSelectedIndex();

					reBuildParamsUI(ms.get(index));
				}
			});
		}

		Method m = api.getApiMethods().get(selectedMethod);
		MethodInfo methodInfo = dataStore.getMethodInfo(dataStore.getCurrentJarData().getService(), m);

		Class[] cls = m.getParameterTypes();
		for (int i = 0; i < cls.length; i++) {
			Class type = cls[i];

			JComponent component = buildRow(paramPanel, type.getName(), methodInfo.getParams().get(i), null);
			inputParamsComponts.put(i + "", component);
		}

		if (this.isVisible()) {
			resizeFlag = !resizeFlag;
			this.setSize(resizeFlag ? this.getWidth() + 1 : this.getWidth() - 1, this.getHeight());
		}
	}

	private JComponent buildRow(JPanel parent, String label, String defaultValue, String[] choices) {
		JComponent content = null;
		defaultValue = String.valueOf(defaultValue);

		if (choices == null) {
			JTextField text = new JTextField();
			text.setText(defaultValue);

			content = text;
		} else if (choices.length > 5) {
			JComboBox list = new JComboBox();
			list.setEditable(false);
			list.setModel(new DefaultComboBoxModel(choices));

			if (defaultValue != null) {
				list.setSelectedItem(defaultValue);
			}

			content = list;
		} else {
			JList list = new JList();
			list.setListData(choices);

			if (defaultValue != null) {
				list.setSelectedValue(defaultValue, true);
			}

			content = list;
		}

		buildRow(parent, label, content);

		return content;
	}

	private void buildRow(JPanel parent, String label, JComponent contentUI) {
		JComponent[] cs = new JComponent[2];

		JLabel label1 = new JLabel("  " + label + ": ");
		cs[0] = label1;

		cs[1] = contentUI;

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(BorderLayout.WEST, cs[0]);
		panel.add(BorderLayout.CENTER, cs[1]);

		parent.add(panel);
	}

	public static void main(String[] args) {
		//move menu bar to the top in Mac OS
		//Doc: http://www.oracle.com/technetwork/articles/javase/javatomac-140486.html
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		DubboTestUI ui = new DubboTestUI();
	}

}
