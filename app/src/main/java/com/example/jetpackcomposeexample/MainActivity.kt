package com.example.jetpackcomposeexample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box // เพิ่ม Box สำหรับ NavHost content
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding // เพิ่ม padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect // เพิ่ม SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector // เพิ่ม ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination // เพิ่ม findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jetpackcomposeexample.ui.theme.JetpackComposeExampleTheme

// Data class สำหรับ Item ใน Bottom Navigation (ใช้ String route)
data class BottomNavigationItem(
    val route: String, // Route เป็น String
    val label: String,
    val icon: ImageVector
    // ไม่ต้องมี composable screen ที่นี่
)

class MainActivity : ComponentActivity() {

    @SuppressLint("RestrictedApi") // อาจไม่จำเป็นอีกต่อไป
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetpackComposeExampleTheme {
                val navController = rememberNavController()
                Log.d("NAV_STRING", "MainActivity - NavController CREATED: ${System.identityHashCode(navController)}")

                Scaffold(
                    topBar = {
                        SmallTopAppBarExample()
                    },
                    bottomBar = {
                        BottomAppBar(navController = navController) // ส่ง navController ตัวเดียวกัน
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Log.d("NAV_STRING", "MainActivity - Scaffold content. Padding: $innerPadding")

                    // NavHost จะใช้ padding ที่ได้จาก Scaffold
                    // และนี่คือส่วนของการ "setGraph" โดยใช้ String routes
                    NavHost(
                        navController = navController,
                        startDestination = "HomePage", // startDestination เป็น String
                        modifier = Modifier.padding(innerPadding) // ใช้ padding จาก Scaffold
                    ) {
                        Log.d("NAV_STRING", "NavHost - Defining composables. NavController ID: ${System.identityHashCode(navController)}")
                        composable("HomePage") { // route เป็น String
                            Log.d("NAV_STRING", "NavHost - Composing HomePage")
                            HomeScreen(navController) // ส่ง navController ถ้า Screen ต้องการ
                        }
                        composable("PersonPage") { // route เป็น String
                            Log.d("NAV_STRING", "NavHost - Composing PersonPage")
                            PersonScreen(navController) // ส่ง navController ถ้า Screen ต้องการ
                        }
                    }

                    SideEffect {
                        Log.d("NAV_STRING", "MainActivity - NavHost SideEffect. Graph nodes: ${navController.graph.nodes.size()}, CurrentDest: ${navController.currentDestination?.route}")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallTopAppBarExample() {
    // เอา Scaffold ที่ซ้อนกันออก
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text("Small Top App Bar")
        }
    )
}

@SuppressLint("RestrictedApi")
@Composable
fun BottomAppBar(navController: NavHostController) {
    Log.d("NAV_LIFECYCLE", "BottomAppBar - Composing. NavController RECEIVED: ${System.identityHashCode(navController)}")

    val bottomNavItems = listOf(
        BottomNavigationItem("HomePage", "Home", Icons.Default.Home),
        BottomNavigationItem("PersonPage", "Person", Icons.Default.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentNavDestination = navBackStackEntry?.destination // เก็บไว้ในตัวแปรเพื่อลดการเรียกซ้ำ

    // LaunchedEffect เพื่อ log สถานะเมื่อมีการเปลี่ยนแปลงที่สำคัญ หรือเมื่อ BottomAppBar ถูก compose
    // key1 และ key2 จะ re-trigger effect นี้ถ้าค่ามันเปลี่ยน
    LaunchedEffect(currentNavDestination, navController) {
        // หน่วงเวลาเล็กน้อย (อาจจะไม่จำเป็นเสมอไป แต่เพื่อทดลอง)
        // เพื่อให้แน่ใจว่า NavHost มีโอกาสทำงานเสร็จสมบูรณ์ในรอบ composition เดียวกัน
        // kotlinx.coroutines.delay(50) // ทดลองใส่ถ้ายังเจอปัญหา timing
        val graphNodeCount = try { navController.graph.nodes.size() } catch (e: IllegalStateException) { -1 }
        val currentDestRoute = currentNavDestination?.route ?: try { navController.currentDestination?.route } catch (e: IllegalStateException) { "ERROR_ACCESSING_DEST_IN_BA" }

        Log.d("NAV_LIFECYCLE", "BottomAppBar - LaunchedEffect. CurrentDest: $currentDestRoute, Graph nodes: $graphNodeCount, NavController ID: ${System.identityHashCode(navController)}")
        if (graphNodeCount <= 0) {
            Log.e("NAV_LIFECYCLE", "BottomAppBar - !!! GRAPH NOT SET or EMPTY in LaunchedEffect !!!")
        }
    }

    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentNavDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    val currentGraphNodes = try { navController.graph.nodes.size() } catch (e: IllegalStateException) { -1 }
                    val currentOnClickDestination = try { navController.currentDestination } catch (e: IllegalStateException) { null }

                    Log.d("NAV_LIFECYCLE", "BottomAppBar - onClick for ${item.label}. NavController ID: ${System.identityHashCode(navController)}")
                    Log.d("NAV_LIFECYCLE", "BottomAppBar - onClick - BEFORE NAV - Graph nodes: $currentGraphNodes, CurrentDest: ${currentOnClickDestination?.route}")

                    // ---- เงื่อนไขการตรวจสอบก่อนเรียก getGraph() (หรือ navigate ที่ต้องใช้ graph) ----
                    if (currentGraphNodes > 0 && currentOnClickDestination != null) {
                        // ณ จุดนี้ เรา "ปลอดภัย" ที่จะ assume ว่า setGraph() ได้ถูกเรียกแล้ว และ graph มี nodes
                        // และ currentDestination ก็ไม่เป็น null
                        navController.navigate(item.route) {
                            // การเรียก .graph ที่นี่ควรจะปลอดภัยแล้ว
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        Log.d("NAV_LIFECYCLE", "BottomAppBar - onClick - Navigation attempted for ${item.route}")
                    } else {
                        Log.e("NAV_LIFECYCLE", "BottomAppBar - Graph not ready or no current destination in onClick. CurrentDest: ${currentOnClickDestination?.route}, Graph nodes: $currentGraphNodes")
                        // คุณอาจจะต้องการแสดง Toast หรือ Snackbar แจ้งผู้ใช้ หรือ disable ปุ่มชั่วคราว
                    }
                }
            )
        }
    }
}

// Composable Screen (สามารถรับ NavController ถ้าต้องการทำ navigation ภายใน screen นั้นๆ)
@Composable
fun HomeScreen(navController: NavHostController) {
    // Box เพื่อจัดกึ่งกลาง Text (ตัวอย่าง)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("HomeScreen")
    }
}

@Composable
fun PersonScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("PersonScreen")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() { // เปลี่ยนชื่อ Preview
    JetpackComposeExampleTheme {
        val navController = rememberNavController() // Mock NavController สำหรับ Preview
        Scaffold(
            topBar = { SmallTopAppBarExample() },
            bottomBar = { BottomAppBar(navController) }, // ส่ง Mock NavController
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Preview Content Area (NavHost not active in preview)")
            }
        }
    }
}