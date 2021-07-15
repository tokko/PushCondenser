package com.tokko.pushcondenser

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestoreSettings
import com.tokko.pushcondenser.databinding.FirestoreListFragmentBinding
import com.tokko.pushcondenser.databinding.RecyclerItemBinding
import com.tokko.pushcondenser.databinding.TextItemBinding
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.viewbinding.BindableItem
import org.w3c.dom.Document

class FirrestoreListFragment(): Fragment() {

    private val viewPool: RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()
    private var _binding: FirestoreListFragmentBinding? = null
    private val binding get () = _binding!!

    val mainAdapter = GroupieAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View? {
        _binding = FirestoreListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirebaseFirestore.getInstance().firestoreSettings = firestoreSettings {
            isPersistenceEnabled = false
        }
        binding.mainRecycler.adapter = mainAdapter
        binding.mainRecycler.layoutManager = LinearLayoutManager(context)
        binding.mainRecycler.setRecycledViewPool(viewPool)
        context?.let { context ->
            mainAdapter.add(RecyclerItem(getPage(), context, ::payForward))
            binding.pageButton.setOnClickListener {
                val snapshot = (mainAdapter.getItem(mainAdapter.itemCount -1) as RecyclerItem).lastDocumentSnapshot
                mainAdapter.add(RecyclerItem(getPage(snapshot), context, ::payForward))
            }
        }

       generateContent()
    }

    private fun generateContent(){
        (0..1000).forEach{
            FirebaseFirestore.getInstance().collection("test").document("$it").set(mapOf("text" to "Entry $it", "id" to it))
        }
    }

    private fun getPage( documentSnapshot: DocumentSnapshot? = null): Query {
        val q = FirebaseFirestore.getInstance().collection("test").orderBy("id", Query.Direction.ASCENDING).limit(20)
          return if(documentSnapshot != null)
                    q.startAfter(documentSnapshot)
              else q
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun payForward(position: Int, lastSnapshot: DocumentSnapshot){
        if(position+1 >= mainAdapter.itemCount){
            return
        }
        val recyclerItem = mainAdapter.getItem(position +1) as RecyclerItem
        val query = getPage(lastSnapshot)
        recyclerItem.query = query
        mainAdapter.notifyDataSetChanged()

    }
}

class RecyclerItem(var query: Query, val context: Context, val payforward: (Int, DocumentSnapshot) -> Unit) : BindableItem<RecyclerItemBinding>() {
    val adapter = GroupieAdapter()
    var lastDocumentSnapshot: DocumentSnapshot? = null

    override fun bind(viewBinding: RecyclerItemBinding, position: Int) {
        viewBinding.pageRecycler.setBackgroundColor(if(position % 2 == 0) Color.GREEN else Color.RED)
        viewBinding.pageRecycler.layoutManager = object: LinearLayoutManager(context){
            override fun canScrollVertically() = true
        }
        viewBinding.pageRecycler.adapter = adapter
        query.addSnapshotListener { value, error ->
            adapter.clear()
            val items = value?.documents?.map { TextItem(it["text"] as String) } ?: emptyList()
            adapter.addAll(
                items
            )
            lastDocumentSnapshot = value?.documents?.lastOrNull()
            lastDocumentSnapshot?.let {
                payforward(position, it)
            }
        }
    }

    override fun getLayout() = R.layout.recycler_item

    override fun initializeViewBinding(view: View) = RecyclerItemBinding.bind(view)

}

class TextItem(val text: String) : BindableItem<TextItemBinding>() {

    override fun bind(viewBinding: TextItemBinding, position: Int) {
        viewBinding.text.text = text
    }

    override fun getLayout() = R.layout.text_item
    override fun initializeViewBinding(view: View) = TextItemBinding.bind(view)

}