/**
 * 数据填充脚本
 * 用于测试分页功能，插入800条Prompt，并创建文件夹结构
 * 
 * 使用方法：
 * node scripts/seed_prompts.js
 */

const BASE_URL = "http://localhost:8080/api";
const USERNAME = "seed_user_" + Math.floor(Math.random() * 1000); // 随机用户名避免冲突
const EMAIL = `seed_${Math.floor(Math.random() * 1000)}@example.com`;
const PASSWORD = "password123";
const TOTAL_PROMPTS = 800;
const BATCH_SIZE = 50; // 每批次插入数量

const folderStructure = [
    {
        name: "工作",
        children: [
            {
                name: "报告",
                children: [
                    { name: "周报" },
                    { name: "月报" }
                ]
            },
            {
                name: "项目",
                children: [
                    { name: "前端" },
                    { name: "后端" }
                ]
            }
        ]
    },
    {
        name: "学习",
        children: [
            {
                name: "编程语言",
                children: [
                    { name: "Python" },
                    { name: "JavaScript" }
                ]
            },
            { name: "算法" }
        ]
    },
    {
        name: "生活",
        children: [
            { name: "旅行" },
            { name: "饮食" }
        ]
    }
];

async function main() {
  console.log("开始执行数据填充...");

  let token = "";
  let userId = "";

  // 1. 注册/登录
  try {
    console.log(`尝试注册用户: ${USERNAME} (${EMAIL})`);
    const registerRes = await fetch(`${BASE_URL}/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        username: USERNAME,
        email: EMAIL,
        password: PASSWORD,
        displayName: "Seed User"
      })
    });

    if (registerRes.ok) {
        const data = await registerRes.json();
        console.log("注册成功，尝试登录...");
    } else {
        console.log("注册可能失败（用户已存在？），尝试直接登录...");
    }

    // 登录获取Token
    const loginRes = await fetch(`${BASE_URL}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email: EMAIL,
        password: PASSWORD
      })
    });

    const loginData = await loginRes.json();
    
    if ((loginData.success || loginData.code === 200) && loginData.data) {
        token = loginData.data.accessToken;
        userId = loginData.data.user.userId;
        console.log("登录成功，获取Token:", token.substring(0, 10) + "...");
    } else {
        throw new Error(`登录响应格式错误: ${JSON.stringify(loginData)}`);
    }

  } catch (error) {
    console.error("认证失败:", error);
    process.exit(1);
  }

  // 2. 创建文件夹
  console.log("开始创建文件夹结构...");
  const folderIds = [null]; // null 表示根目录

  async function createFoldersRecursive(nodes, parentId = null) {
    for (const node of nodes) {
        try {
            const res = await fetch(`${BASE_URL}/folders`, {
                method: "POST",
                headers: { 
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`,
                    "X-User-Id": userId
                },
                body: JSON.stringify({
                    name: node.name,
                    parentId: parentId
                })
            });
            
            const data = await res.json();
            if ((data.success || data.code === 200) && data.data) {
                const folderId = data.data.id;
                folderIds.push(folderId);
                console.log(`创建文件夹成功: ${node.name} (ID: ${folderId})`);
                
                if (node.children && node.children.length > 0) {
                    await createFoldersRecursive(node.children, folderId);
                }
            } else {
                console.error(`创建文件夹失败: ${node.name}`, data);
            }
        } catch (err) {
            console.error(`创建文件夹出错: ${node.name}`, err);
        }
        // 稍微延时避免过快
        await new Promise(r => setTimeout(r, 100));
    }
  }

  await createFoldersRecursive(folderStructure);
  console.log(`文件夹创建完成，共收集 ${folderIds.length} 个位置（含根目录）`);

  // 3. 生成数据并插入
  console.log(`准备插入 ${TOTAL_PROMPTS} 条 Prompt 数据...`);
  
  const categories = ['通用', '写作', '编程', '分析', '创意', '营销'];
  const tagsPool = ['测试', '示例', '开发', '生产力', '工具', '学习', 'AI', '助手'];

  let successCount = 0;
  let failCount = 0;

  for (let i = 0; i < TOTAL_PROMPTS; i += BATCH_SIZE) {
    const batchPromises = [];
    const end = Math.min(i + BATCH_SIZE, TOTAL_PROMPTS);
    
    console.log(`正在处理第 ${i + 1} 到 ${end} 条...`);

    for (let j = i; j < end; j++) {
        const randomCategory = categories[Math.floor(Math.random() * categories.length)];
        const randomTags = [
            tagsPool[Math.floor(Math.random() * tagsPool.length)],
            tagsPool[Math.floor(Math.random() * tagsPool.length)]
        ];
        const randomFolderId = folderIds[Math.floor(Math.random() * folderIds.length)];
        
        const promptData = {
            title: `测试 Prompt ${j + 1} - ${randomCategory}`,
            content: `这是一个用于测试分页功能的 Prompt 内容。编号：${j + 1}。\n\n这里有一些随机内容：${Math.random().toString(36).substring(7)}`,
            tags: Array.from(new Set(randomTags)), // 去重
            category: randomCategory,
            isPublic: false,
            folderId: randomFolderId // 随机分配文件夹
        };

        const promise = fetch(`${BASE_URL}/prompts`, {
            method: "POST",
            headers: { 
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
                "X-User-Id": userId
            },
            body: JSON.stringify(promptData)
        }).then(res => {
            if (res.ok) return { success: true };
            return { success: false, status: res.status };
        }).catch(err => ({ success: false, error: err }));

        batchPromises.push(promise);
    }

    const results = await Promise.all(batchPromises);
    results.forEach(r => {
        if (r.success) successCount++;
        else failCount++;
    });
    
    // 简单的延时，避免请求过快
    await new Promise(resolve => setTimeout(resolve, 100));
  }

  console.log("========================================");
  console.log(`执行完成！`);
  console.log(`成功插入: ${successCount}`);
  console.log(`失败: ${failCount}`);
  console.log("========================================");
  console.log(`请在前端使用账户登录以查看数据：`);
  console.log(`Email: ${EMAIL}`);
  console.log(`Password: ${PASSWORD}`);
}

main();
