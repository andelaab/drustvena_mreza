package ba.sum.fpmoz.drustvenamreza

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import ba.sum.fpmoz.drustvenamreza.ui.theme.data.model.PostsFragment
import ba.sum.fpmoz.drustvenamreza.ProfileFragment

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PostsFragment())
            .commit()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_posts -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, PostsFragment())
                        .commit()
                    true
                }
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer,
                            ba.sum.fpmoz.drustvenamreza.ProfileFragment()
                        )
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}
