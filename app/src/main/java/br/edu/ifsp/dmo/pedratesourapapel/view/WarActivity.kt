package br.edu.ifsp.dmo.pedratesourapapel.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo.pedratesourapapel.R
import br.edu.ifsp.dmo.pedratesourapapel.databinding.ActivityWarBinding
import br.edu.ifsp.dmo.pedratesourapapel.model.Constants
import br.edu.ifsp.dmo.pedratesourapapel.model.Paper
import br.edu.ifsp.dmo.pedratesourapapel.model.Player
import br.edu.ifsp.dmo.pedratesourapapel.model.Rock
import br.edu.ifsp.dmo.pedratesourapapel.model.Scissors
import br.edu.ifsp.dmo.pedratesourapapel.model.War
import br.edu.ifsp.dmo.pedratesourapapel.model.Weapon


class WarActivity()  : AppCompatActivity() {

    private lateinit var binding: ActivityWarBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var war: War

    private var weaponPlayer1: Weapon? = null
    private var weaponPlayer2: Weapon? = null

    private var botGame: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        openBundle()
        updateUI()
        configListener()
        configResultLauncher()
    }

    private fun battle() {
        val winner: Player?

        if(botGame){
            weaponPlayer2 = botWeaponSelection()
        }

        if (weaponPlayer1 != null && weaponPlayer2 != null) {

            winner = war.toBattle(weaponPlayer1!!, weaponPlayer2!!)

            if (winner != null) {
                Toast.makeText(
                    this,
                    "${getString(R.string.winner)} ${winner.name}",
                    Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.draw),
                    Toast.LENGTH_LONG
                ).show()
            }

            weaponPlayer1 = null
            weaponPlayer2 = null

            updateScoreBoard()

            if (!war.has_battles()) {
                proclaimWinner()
            }

        } else {
            val name: String = if (weaponPlayer1 == null) {
                war.opponent1.name
            } else {
                war.opponent2.name
            }

            Toast.makeText(
                this,
                "$name${getString(R.string.choose_gun_player)}",
                Toast.LENGTH_LONG
            ).show()

        }
    }

    private fun configListener() {
        binding.buttonWeapon1.setOnClickListener { startSelectionActivity(1) }
        if(botGame){
            binding.buttonWeapon2.visibility = View.GONE
        } else {
            binding.buttonWeapon2.setOnClickListener { startSelectionActivity(2) }
        }

        binding.buttonFight.setOnClickListener { battle() }
        binding.buttonClose.setOnClickListener { finish() }
    }

    private fun configResultLauncher() {
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                val extras = result.data?.extras

                if (extras != null) {
                    val number = extras.getInt(Constants.KEY_PLAYER_NUMBER)
                    val chosenWeapon: Weapon = extras.getSerializable(Constants.KEY_WEAPON, Weapon::class.java) as Weapon

                    if (number == 1)
                        weaponPlayer1 = chosenWeapon

                    if (number == 2)
                        weaponPlayer2 = chosenWeapon

                }

            }
        }
    }

    private fun openBundle() {
        val extras = intent.extras
        if (extras != null) {
            val p1 = extras.getString(Constants.KEY_PLAYER_1)
            val p2 = extras.getString(Constants.KEY_PLAYER_2)
            val number = extras.getInt(Constants.KEY_ROUNDS)
            botGame = extras.getBoolean(Constants.KEY_BOT_GAME)
            war = War(number, p1!!, p2!!)
        }
    }

    private fun proclaimWinner() {
        val str = "${war.getWinner().name} ${getString(R.string.won_the_match)}"

        binding.buttonWeapon1.visibility = View.GONE
        binding.buttonWeapon2.visibility = View.GONE
        binding.buttonFight.visibility = View.GONE
        binding.buttonClose.visibility = View.VISIBLE
        binding.textviewReport.visibility = View.VISIBLE
        binding.textviewReport.text = str
    }

    private fun startSelectionActivity(number: Int) {
        val name = if (number == 1) war.opponent1.name else war.opponent2.name
        val mIntent = Intent(this, SelectionActivity::class.java)

        mIntent.putExtra(Constants.KEY_PLAYER_NUMBER, number)
        mIntent.putExtra(Constants.KEY_PLAYER_NAME, name)

        resultLauncher.launch(mIntent)
    }

    private fun updateScoreBoard() {
        binding.textviewScore1.text = "${war.opponent1.points}"
        binding.textviewScore2.text = "${war.opponent2.points}"
    }

    private fun updateUI() {
        val str = "${war.opponent1.name} X ${war.opponent2.name}"
        actionBar?.setTitle(str)

        binding.labelPlayer1.text = war.opponent1.name
        binding.labelPlayer2.text = war.opponent2.name

        updateScoreBoard()

        binding.buttonWeapon1.text = "${war.opponent1.name} ${getString(R.string.gun_selection)}"
        binding.buttonWeapon2.text = "${war.opponent2.name} ${getString(R.string.gun_selection)}"
    }

    private fun botWeaponSelection() : Weapon {

        val random = (0..2).random()
        return when (random) {
            0 -> Rock
            1 -> Paper
            else -> Scissors
        }
    }
}