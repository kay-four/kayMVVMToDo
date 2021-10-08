package com.example.kayfourtodo.ui.tasks


import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kayfourtodo.R
import com.example.kayfourtodo.data.SortOrder
import com.example.kayfourtodo.data.Tasks
import com.example.kayfourtodo.databinding.FragmentAllTasksBinding
import com.example.kayfourtodo.util.exhaustive
import com.example.kayfourtodo.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AllTasksFragment:Fragment(R.layout.fragment_all_tasks), TasksAdapter.OnItemClickListener{

    private val viewModel: TasksViewModel by viewModels()
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAllTasksBinding.bind(view)
        val tasksAdapter = TasksAdapter(this)

        binding.apply {
            tasksRecycler.apply {
                adapter = tasksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            ItemTouchHelper(object:ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                            return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val task = tasksAdapter.currentList[viewHolder.adapterPosition]
                viewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(tasksRecycler)

            addTaskFab.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }

        }

        setFragmentResultListener("add_edit_request"){_,bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }


       viewLifecycleOwner.lifecycleScope.launchWhenStarted {
           viewModel.tasksEvent.collect { event ->
               when(event){
                   is TasksViewModel.TasksEvent.ShowUndoDeleteTaskMessage ->{
                       Snackbar.make(requireView(),"Task has been deleted",Snackbar.LENGTH_LONG)
                           .setAction("UNDO"){
                               viewModel.onUndoDeleteClick(event.task)
                           }.show()
                   }
                  is TasksViewModel.TasksEvent.NavigateToAddScreen -> {
                      val action = AllTasksFragmentDirections.actionAllTasksFragmentToAddEditTaskFragment(null,"Add Task")
                      findNavController().navigate(action)

                  }
                   is TasksViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                       val action = AllTasksFragmentDirections.actionAllTasksFragmentToAddEditTaskFragment(event.task,"Edit Task")
                       findNavController().navigate(action)
                   }
                   is TasksViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
                       Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                   }
                   is TasksViewModel.TasksEvent.NavigateToDeleteAllCompletedScreen -> {
                       val action = AllTasksFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                       findNavController().navigate(action)
                   }
               }.exhaustive
           }
       }

        setHasOptionsMenu(true)

        viewModel.tasks.observe(viewLifecycleOwner) {
            tasksAdapter.submitList(it)
        }

    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_all_tasks, menu)

        val searchItem = menu.findItem(R.id.action_search)
         searchView = searchItem.actionView as androidx.appcompat.widget.SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery,false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
                    }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked=
                viewModel.preferencesFlow.first().hideCompleted
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       return when(item.itemId){
            R.id.action_filter_by_name -> {
                viewModel.onSortOrderSelected(requireContext(),SortOrder.BY_NAME)
                true
            }

           R.id.action_filter_by_date_created ->{
               viewModel.onSortOrderSelected(requireContext(),SortOrder.BY_DATE)

               true
           }

           R.id.action_hide_completed_tasks ->{
               item.isChecked = !item.isChecked
               viewModel.onHideCompletedClick(requireContext(),item.isChecked)
               true
           }

           R.id.action_delete_all_copleted_tasks ->{
               viewModel.onDeleteAllCompletedClick()

               true
           }
           else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }

    override fun onItemClick(task: Tasks) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Tasks, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task,isChecked)
    }


}