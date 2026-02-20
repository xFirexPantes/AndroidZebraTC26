package com.example.scanner.ui.navigation

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scanner.R
import com.example.scanner.modules.SecureStorage
import com.example.scanner.modules.UserItem
import com.example.scanner.ui.base.BaseFragment
import timber.log.Timber


class AdminFragment : BaseFragment() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var btnRefresh: Button
    private lateinit var btnClearStorage: Button
    private lateinit var storage: SecureStorage
    private lateinit var adapter: UsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.admin_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvUsers = view.findViewById(R.id.rvUsers)
        btnRefresh = view.findViewById(R.id.btnRefresh)
        btnClearStorage = view.findViewById(R.id.btnClearStorage)
        storage = SecureStorage(requireContext())

        // Инициализация адаптера
        adapter = UsersAdapter(mutableListOf())
        rvUsers.adapter = adapter
        rvUsers.layoutManager = LinearLayoutManager(requireContext())

        // Настройка свайпа
        setupSwipeToDelete()

        loadUsers()

        btnRefresh.setOnClickListener { loadUsers() }
        btnClearStorage.setOnClickListener { showClearStorageDialog() }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadUsers() {
        val allUids = storage.debugGetAllUids()
        val users = allUids.map { uid ->
            val creds = storage.getCredentials(uid)
            UserItem(uid, creds?.login, creds?.password)
        }.toMutableList()

        adapter.users.clear()
        adapter.users.addAll(users)
        adapter.notifyDataSetChanged()
    }

    // Настройка свайпа вправо для удаления
    private fun setupSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val user = adapter.users[position]
                deleteUser(user)
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(rvUsers)
    }

    private fun deleteUser(user: UserItem) {
        try {
            storage.removeCredentials(user.uid)  // Предполагаемый метод для удаления по UID
            adapter.removeItem(adapter.users.indexOf(user))
            showToast("Пользователь ${user.uid} удалён")
        } catch (e: Exception) {
            showToast("Ошибка при удалении: ${e.message}")
            Timber.tag("AdminFragment").e(e, "Ошибка удаления пользователя")
        }
    }

    private fun showClearStorageDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Очистить хранилище")
            .setMessage("Вы уверены, что хотите удалить все данные? Это действие нельзя отменить.")
            .setPositiveButton("Удалить") { _, _ -> clearStorage() }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun clearStorage() {
        try {
            storage.clearAll()
            showToast("Хранилище очищено")
            loadUsers()
        } catch (e: Exception) {
            showToast("Ошибка при очистке: ${e.message}")
            Timber.tag("AdminFragment").e(e, "Ошибка очистки хранилища")
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}


class UsersAdapter(
    val users: MutableList<UserItem>
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    fun removeItem(position: Int) {
        users.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUid: TextView = itemView.findViewById(R.id.tvUid)
        private val tvLogin: TextView = itemView.findViewById(R.id.tvLogin)
        private val tvPassword: TextView = itemView.findViewById(R.id.tvPassword)


        @SuppressLint("SetTextI18n")
        fun bind(user: UserItem) {
            tvUid.text = "UID: ${user.uid}"
            tvLogin.text = "Логин: ${user.login ?: "—"}"
            tvPassword.text = "Пароль: ${user.password ?: "—"}"
        }
    }
}
